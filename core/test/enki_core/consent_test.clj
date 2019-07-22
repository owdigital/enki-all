;; This file is part of Enki.
;;
;; Copyright © 2016 - 2019 Oliver Wyman Ltd.
;;
;; Enki is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Enki is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Enki.  If not, see <https://www.gnu.org/licenses/>
;;
(ns enki-core.consent-test
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [clj-http.cookies :as cookies]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [defroutes GET POST]]
            [enki-core.agent :as agent]
            [enki-core.hydra.api :as hapi]
            [enki-core.hydra.consent :as hydra-consent]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.store.sql :as sql-store]
            [enki-core.system :refer [instance] :as system]
            [enki-core.time :as time]
            [enki-core.util :refer [join-url base64encode]]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :as html]
            [reloaded.repl :as repl]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import (java.util UUID)
           (net.lshift.spki.suiteb PrivateSigningKey)
           (net.lshift.spki.convert ConvertUtils)))

(def client-port 9030)
(def agent-port 3010)
(def app-port 3000)

(def callback-url (str "http://localhost:" client-port "/callback"))

(def hydra-client-id (env :hydra-client-id))
(def hydra-client-secret (env :hydra-client-secret))
(def core-url (str "http://localhost:" app-port))
(def agent-url (str "http://localhost:" agent-port))

(def consumer-app-id "dummy-app-actual-this-test")
(def scopes #{"openid" "offline" "hydra.clients" "dummy" "dummy2"})
(def shorter-scopes (disj scopes "dummy2" "dummy"))

(defn- make-location
  []
  (-> (agent/client-post (join-url agent-url "data")
                         {:type "pii-data"
                          :id (str (UUID/randomUUID))
                          :piiType "surname"
                          :subjectId "alpha"
                          :processorId "beta"
                          :value "foo"})
      :body
      :location))

(defonce test-location (atom nil))
(defonce test-location2 (atom nil))
(defonce oauth-token (atom nil))

(def consent-state "aaaaaaaaaaaaaaaaa")
(def nonce "abcdabcdabcdabcd")

(def hydra-url (env :hydra-server-url))
(def admin-login (env :hydra-admin-login))
(def admin-password (env :hydra-admin-password))

(defn test-system
  []
  (repl/set-init! #(instance {::system/dev? true
                              ::system/http-port app-port
                              ::system/database-url (env :core-database-url)}))
  (repl/go)
  (reset! oauth-token (hapi/get-access-token admin-login admin-password))
  (hapi/create-consent-client! hydra-client-id @oauth-token hydra-client-secret))

(defroutes my-consumer-app
  (GET "/callback" {:keys [query-params]}
    {:body (json/generate-string query-params)
     :status 200}))

(defroutes my-agent-routes
  (POST "/data" {:keys [body]}
    (let [req   (json/parse-string (slurp body) true)]
      {:body (json/generate-string {:location (format "%s-%s-%s" (:type req) (:subjectId req) (:processorId req))})
       :status 200}))
  (POST "/access" {:keys [body]}
    {:body "true"
     :status 200}))

(defn system-fixture
  [f]
  (let [consumer-server  (jetty/run-jetty (wrap-params my-consumer-app) {:port client-port :join? false})
        agent-server     (jetty/run-jetty (wrap-params my-agent-routes) {:port agent-port :join? false})]
    (try
      (test-system)
      (f)
      (finally
        (.stop agent-server)
        (.stop consumer-server)
        (repl/stop)))))

(use-fixtures :once system-fixture)

(defn- get-html-node
  [body id]
  (-> (html/select (html/html-snippet body) [id])
      first))

(defn- do-get
  ([url cs] (do-get url cs {}))
  ([url cs xs]
   (try+
    (let [req (merge {:redirect-strategy :none :cookie-store cs :cookie-policy :standard} xs)]
      (loop [resp (client/get url req)]
        (if (contains? #{301 302} (:status resp))
          (let [location (-> resp :headers (get "location"))]
            (log/infof "Redirected to %s" location)
            (if (str/includes? location "localhost:9020") ; to co-operate with hydra_test
              (let [new-location (str/replace location "localhost:9020" "localhost:3000")]
                (log/infof "Rewrote %s to %s" location new-location)
                (recur (client/get new-location req)))
              (recur (client/get location req))))
          resp)))
    (catch Object _
      (log/errorf "Error while trying to GET %s with %s" url (pr-str xs))
      (throw+)))))

(defn- get-consent-request
  "Hit the Hydra auth endpoint, which redirects to enki consent URL.
  As the user is not logged in, the user is then redirected to the login page."
  [cs scopes]
  (.clear cs) ; because otherwise we might already be logged in
  (let [auth-url  (join-url hydra-url "/oauth2/auth")
        response  (do-get auth-url cs {:query-params {:redirect_uri callback-url
                                                      :scope (str/join " " scopes)
                                                      :state consent-state
                                                      :response_type "code"
                                                      :client_id consumer-app-id
                                                      :nonce nonce}})]
    (is (= 200 (:status response)))
    (get-html-node (:body response) :form)))

(defn- do-enki-post
  ([cs fragment form-params] (do-enki-post cs fragment form-params {}))
  ([cs fragment form-params params]
   (let [cookies   (cookies/get-cookies cs)
         url       (join-url core-url fragment)
         session   (get-in cookies ["ring-session" :value])]
     (is (some? session))
     (try+
      (client/post url (merge params {:form-params form-params
                                      :headers {"cookie" session}
                                      :cookie-store cs
                                      :cookie-policy :standard}))
      (catch Object _
        (log/errorf "Error while trying to POST %s with %s" url (pr-str form-params))
        (throw+))))))

(defn- do-register
  [cs username password]
  (let [response   (do-get (str core-url "/register") cs)
        csrf-token (-> (get-html-node (:body response) :input#__anti-forgery-token)
                       (get-in [:attrs :value]))]
    (is (= 200 (:status response)))
    (is (not (str/blank? csrf-token)))
    (let [response2   (do-enki-post cs "/register" {"username" username
                                                    "password" password
                                                    "password-confirm" password
                                                    "__anti-forgery-token" csrf-token})]
      (is (= 200 (:status response2))))
    csrf-token))

(defn- do-consent-post
  "Agree to scopes and POST to consent which redirects to Hydra which redirects
  to callback url. The callback URL stands in for consumer app."
  [cs csrf-token url assertions]
  (let [params        {"__anti-forgery-token" csrf-token
                       "dummy" (str (:md-1 assertions))}
        response      (do-enki-post cs
                                    url
                                    params
                                    {:trace-redirects true
                                     :redirect-strategy :lax})
        body          (json/parse-string (:body response) true)]
    (is (= 200 (:status response)))
    (is (= #{:code :scope :state} (set (keys body))))
    (is (not (str/blank? (:code body))))
    (is (= consent-state (:state body)))
    (is (= (conj shorter-scopes (str (:md-1 assertions)))
           (set (str/split (:scope body) #" "))))
    (:code body)))

(defn- setup-oauth-client
  [cs]
  (let [consumer-secret (hapi/create-consumer-app! consumer-app-id [callback-url] scopes @oauth-token)]
    (hapi/create-app-policy! "policy-actual" hydra-client-id @oauth-token)
    consumer-secret))

(defn- setup-assertions
  [processor-id user-id]
  (reset! test-location (make-location))
  (reset! test-location2 (make-location))
  (let [priv-key  (PrivateSigningKey/generate)
        key-bytes (ConvertUtils/toBytes (.getPublicKey priv-key))
        response  (client/post
                   (str core-url "/api/registerkey")
                   {:form-params
                    {"bank_name" processor-id
                     "signing_key" (base64encode key-bytes)
                     "consus_user" "test-agent@test.labshift.io"
                     "agent_url" agent-url
                     "oauth_client_id" consumer-app-id}})]
    (is (= (:status response) 200))
    (with-transaction (:db repl/system) [tx]
      (let [bank-id   (-> (:body response)
                          (json/parse-string true)
                          :result
                          :id
                          (UUID/fromString))
            pii-type  (store/insert-pii-type! (:db repl/system) tx "dummy" "Dummy PII Type")
            pii-type2 (store/insert-pii-type! (:db repl/system) tx "dummy2" "Dummy PII Type 2")
            md-1      (store/insert-metadata-assertion! (:db repl/system)
                                                        tx
                                                        bank-id
                                                        {:subject "alice"
                                                         :pii_type (:id pii-type)
                                                         :location @test-location
                                                         :created (time/utc-now)
                                                         :signature "some-signature"})
            md-2      (store/insert-metadata-assertion! (:db repl/system)
                                                        tx
                                                        bank-id
                                                        {:subject "alice"
                                                         :pii_type (:id pii-type2)
                                                         :location @test-location2
                                                         :created (time/utc-now)
                                                         :signature "some-signature"})]
        (j/insert! tx :user_association {:user_id user-id
                                         :bank_id bank-id
                                         :bank_user_id "alice"})
        {:bank-id bank-id
         :md-1 (:id md-1)
         :md-2 (:id md-2)}))))

(defn- verify-share-locations
  [token assertions]
  (let [response  (client/get (join-url core-url "api/sharelocations")
                              {:oauth-token token})
        body      (json/parse-string (:body response) true)]
    (is (= [{:location @test-location :id (str (:md-1 assertions)) :pii "dummy"}]
           body))))

(deftest ^:integration test-consent-flow
  (let [bank-name        "sample-bank"
        username         "alice"
        passwd           "baz"
        cs               (cookies/cookie-store)
        _                (do-register cs username passwd)
        user             (with-transaction (:db repl/system) [tx]
                           (store/get-user-by-name (:db repl/system) tx username false))
        consumer-secret  (setup-oauth-client cs)
        assertions       (setup-assertions bank-name (:id user))
        form             (get-consent-request cs scopes)
        login-url        (get-in form [:attrs :action])
        csrf-token       (-> (html/select form [:input#__anti-forgery-token]) first (get-in [:attrs :value]))]
    (is (some? form))
    (is (not (str/blank? login-url)))
    (let [response   (do-enki-post cs
                                   login-url
                                   {"username" username
                                    "password" passwd
                                    "__anti-forgery-token" csrf-token}
                                   {:trace-redirects true
                                    :redirect-strategy :lax})
          form       (get-html-node (:body response) :form)
          url        (get-in form [:attrs :action])]
      (is (= 200 (:status response)))
      (is (some? form))
      (is (not (str/blank? url)))
      (let [code        (do-consent-post cs csrf-token url assertions)
            auth-res    (hapi/code->access-token code consumer-app-id consumer-secret callback-url)
            user-info   (hydra-consent/get-unsigned-id-token auth-res @oauth-token)]
        (is (= #{:aud :sub :iss :name :exp :auth_time :nonce :iat}
               (set (keys user-info))))
        (is (= (:id user) (UUID/fromString (:sub user-info))))
        (is (= consumer-app-id (:aud user-info)))
        (is (= nonce (:nonce user-info)))
        (is (= (conj shorter-scopes (str (:md-1 assertions)))
               (set (str/split (:enki-core.hydra.spec/scope auth-res) #" "))))
        (verify-share-locations (:enki-core.hydra.spec/access_token auth-res)
                                assertions)))))
