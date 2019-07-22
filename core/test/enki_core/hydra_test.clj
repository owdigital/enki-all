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
(ns enki-core.hydra-test
  (:require [clj-http.client :as client]
            [clj-http.cookies :as cookies]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [compojure.core :refer [defroutes GET]]
            [enki-core.hydra.api :refer :all]
            [enki-core.hydra.consent :as consent]
            [enki-core.util :refer [join-url]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty])
  (:import (java.net URI)
           (org.apache.http NameValuePair)
           (org.apache.http.client.utils URLEncodedUtils)))

(defn random-id [prefix] (str prefix "-" (java.util.UUID/randomUUID)))
(def consent-client-id (random-id "consent-client-id"))
(def consumer-app-id (random-id "consumer-app-id"))
(def consent-policy-id (random-id "consent-policy-id"))
(def user-policy-id (random-id "user-policy-id"))

(def hydra-client-id (random-id "dummy-consent-app"))
(def callback-url "http://localhost:9020/callback")

(def scopes #{"openid" "offline" "hydra.clients" "foo.test" "bar" "spam" "eggs"})

(def consent-state "aaaaaaaaaaaaaaaaa")
(def user-id "user:12345:dandean")
(def admin-login (env :hydra-admin-login))
(def admin-password (env :hydra-admin-password))

(def ^:dynamic *oauth-token*)

(defroutes my-dummy-app
  (GET "/consent" [] "hello")
  (GET "/callback" [] "goodbye"))

(defn server-fixture
  [f]
  (try
    (binding [*oauth-token* (get-access-token admin-login admin-password)]
      (let [server  (jetty/run-jetty
                     #'my-dummy-app
                     {:port 9020 :join? false})]
        (try
          (f)
          (finally
            (.stop server)))))
    (catch Exception e
      (is (not e)))))

(use-fixtures :once server-fixture)

(deftest ^:integration consent-client-flow-test
  (testing "Creating consent client"
    (let [client-secret (create-consent-client! consent-client-id *oauth-token*)]
      (is (not (nil? client-secret)))))

  (testing "Creating app policy"
    (let [result (create-app-policy! consent-policy-id consent-client-id *oauth-token*)]
      (is (nil? result))))

  (testing "Deleting consent client"
    (let [result (delete-client! consent-client-id *oauth-token*)]
      (is (nil? result))))

  (testing "Deleting consent policy"
    (let [result (delete-policy! consent-policy-id *oauth-token*)]
      (is (nil? result)))))

(deftest ^:integration consumer-app-flow-test
  (testing "Creating consumer app"
    (let [client-secret (create-consumer-app! consumer-app-id
                                              [callback-url]
                                              scopes
                                              *oauth-token*)]
      (is (not (nil? client-secret)))))

  (testing "Getting client info"
    (let [client-info (get-client consumer-app-id *oauth-token*)]
      (is (not (nil? client-info)))))

  (testing "Updating consumer app"
    (let [redirect-uris       ["callbacks"]
          scopes              #{"new" "scopes"}
          update-result       (update-consumer-app! consumer-app-id
                                                    redirect-uris
                                                    scopes
                                                    *oauth-token*)
          result-redirect-uri (get update-result :enki-core.hydra.spec/redirect_uris)
          result-scopes       (get update-result :enki-core.hydra.spec/scope)]
      (is (= result-redirect-uri redirect-uris))
      (is (= (set (str/split result-scopes #" "))
             (set/union scopes
                        #{"openid" "offline" "hydra.clients"})))))

  (testing "Creating user policy"
    (let [result (create-user-policy! user-id user-policy-id *oauth-token*)]
      (is (nil? result))))

  (testing "Getting policy info"
    (let [policy-info (get-policy user-policy-id *oauth-token*)]
      (is (not (nil? policy-info)))))

  (testing "Updating policy info"
    (let [actions         ["new action"]
          effect          "effect"
          subjects        ["user"]
          update-result   (update-policy! user-policy-id actions effect subjects *oauth-token*)
          result-actions  (get update-result :enki-core.hydra.spec/actions)
          result-effect   (get update-result :enki-core.hydra.spec/effect)
          result-subjects (get update-result :enki-core.hydra.spec/subjects)]
      (is (= result-actions actions))
      (is (= result-effect effect))
      (is (= result-subjects subjects))))

  (testing "Deleting consumer app"
    (let [result (delete-client! consumer-app-id *oauth-token*)]
      (is (nil? result))))

  (testing "Deleting user policy"
    (let [result (delete-policy! user-policy-id *oauth-token*)]
      (is (nil? result)))))

(defn- get-consent-request
  [cs scopes]
  (let [auth-url (join-url (env :hydra-server-url) "/oauth2/auth")]
    (client/get auth-url
                {:query-params {:redirect_uri callback-url
                                :scope (str/join " " scopes)
                                :state consent-state
                                :response_type "code"
                                :client_id consumer-app-id
                                :nonce "abcdabcdabcdabcd"}
                 :force-redirects true
                 :cookie-store cs
                 :cookie-policy :standard})))

(defn- get-url-params
  [x]
  (let [xs (URLEncodedUtils/parse (URI. x) "UTF-8")]
    (into {}
          (map (fn [^NameValuePair y]
                 [(.getName y)
                  (.getValue y)])
               xs))))

(defn generate-redirect-url
  [cs token-holder scopes]
  (let [consent-req        (get-consent-request cs scopes)
        params             (get-url-params (first (:trace-redirects consent-req)))
        challenge          (get params "challenge")
        data               {:name "Alice"}
        verify-result      (consent/verify-consent-challenge
                            token-holder
                            challenge)
        consent            (consent/generate-consent-response
                            token-holder
                            verify-result
                            user-id
                            scopes
                            {}
                            data)
        redir-url          (str (:redir verify-result) "&consent=" consent)]
    redir-url))

(deftest ^:integration user-consent-flow-test
  (let [cs               (cookies/cookie-store)
        client-secret    (create-consent-client! hydra-client-id *oauth-token*)
        token-holder     (consent/token-instance
                          hydra-client-id
                          client-secret)
        consumer-secret  (create-consumer-app! consumer-app-id [callback-url] scopes *oauth-token*)]

    (create-app-policy! (random-id "app-policy-id") hydra-client-id *oauth-token*)

    (testing "Verify consent"
      (let [params        (get-url-params (first (:trace-redirects (get-consent-request cs scopes))))
            challenge     (get params "challenge")
            verify-result (consent/verify-consent-challenge
                           token-holder
                           challenge)
            result-keys   (keys verify-result)]
        (is (= (set result-keys) #{:aud :exp :jti :redir :scp}))))

    (testing "Resolve consent"
      (let [params         (get-url-params (first (:trace-redirects (get-consent-request cs scopes))))
            challenge      (get params "challenge")
            verify-result  (consent/verify-consent-challenge
                            token-holder
                            challenge)
            data           {:name "Alice"}
            consent        (consent/generate-consent-response
                            token-holder
                            verify-result
                            user-id
                            scopes
                            {}
                            data)]
        (is (string? consent))
        (is (pos? (count consent)))))

    (testing "Getting Auth token with all scopes allowed"
      (let [new-scopes scopes
            redir-url  (generate-redirect-url cs token-holder new-scopes)
            res        (client/get redir-url
                                   {:force-redirects true
                                    :cookie-store cs
                                    :cookie-policy :standard})
            params     (get-url-params (first (:trace-redirects res)))]
        (is (= consent-state (get params "state")))
        (is (pos? (count (get params "code"))))
        (let [res    (consent/get-auth-token consumer-app-id
                                             consumer-secret
                                             (get params "code")
                                             callback-url)
              res2   (consent/validate-token
                      token-holder
                      (:token res))]
          (is (= #{:token :expires-at}
                 (set (keys res))))
          (is (= #{:active :client_id :sub :exp :iat :aud :iss :scope}
                 (set (keys res2))))
          (is (:active res2))
          (is (= (set (str/split (:scope res2) #" "))
                 (set/union scopes
                            #{"openid" "offline" "hydra.clients"})))
          (is (= user-id (:sub res2)))
          (is (= consumer-app-id (:client_id res2))))))

    (testing "Getting Auth token with some scopes that are not allowed"
      (let [new-scopes (conj scopes "baz")
            redir-url  (generate-redirect-url cs token-holder new-scopes)
            res        (client/get redir-url
                                   {:force-redirects true
                                    :cookie-store cs
                                    :cookie-policy :standard})
            params     (get-url-params (first (:trace-redirects res)))]
        (is (zero? (count (get params "code"))))))

    (testing "Getting Auth token with some claims"
      (let [new-scopes (conj scopes "bar.test")
            redir-url  (generate-redirect-url cs token-holder new-scopes)
            res        (client/get redir-url
                                   {:force-redirects true
                                    :cookie-store cs
                                    :cookie-policy :standard})
            params     (get-url-params (first (:trace-redirects res)))]
        (is (pos? (count (get params "code"))))))

    (testing "Getting Auth token with some not allowed claims"
      (let [new-scopes (conj scopes "foo.notest")
            redir-url  (generate-redirect-url cs token-holder new-scopes)
            res        (client/get redir-url
                                   {:force-redirects true
                                    :cookie-store cs
                                    :cookie-policy :standard})
            params     (get-url-params (first (:trace-redirects res)))]
        (is (zero? (count (get params "code"))))))))

(deftest ^:integration list-clients-test
  (testing "Listing clients test"
    (let [result (list-clients *oauth-token*)]
      (is (not (nil? result))))))
