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
(ns enki-core.handler-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.java.jdbc :as j]
            [cheshire.core :as json]
            [com.stuartsierra.component :as component]
            [enki-core.handler :as handler]
            [enki-core.handlebars :as handlebars]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.migrator :as migrator]
            [enki-core.store.core :as store]
            [enki-core.store.sql :as sql-store]
            [enki-core.time :as time]
            [enki-core.util :refer [base64encode]]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :as html]
            [reloaded.repl :as repl]
            [ring.mock.request :as mock]
            [ring.util.response :as resp])
  (:import (net.lshift.spki.suiteb PrivateSigningKey Sequence Signed Action)
           (net.lshift.spki.convert ConvertUtils)
           (java.util UUID)
           (net.lshift.enki MetadataAssertion PiiType ShareAssertion SharingPurpose)))

(def ^:dynamic *app*)

(defn test-system
  [_]
  (let [registry (handlebars/setup-handlebars! false)]
    (component/system-map
     :db (sql-store/instance (env :database-url))
     :migrator (component/using
                (migrator/instance)
                [:db])
     :registry registry
     :context {:webpackBaseUrl "build"
               :bundles (handlebars/get-bundles-files false)})))

(defn system-fixture
  [f]
  (repl/set-init! #(test-system {}))
  (repl/go)
  (binding [*app* (handler/make-app (select-keys repl/system [:db :registry :context]))]
    (try
      (f)
      (finally
        (with-transaction (:db repl/system) [tx]
          (j/delete! tx :user_association [])
          (j/delete! tx :share_assertion_revocation [])
          (j/delete! tx :metadata_assertion_revocation [])
          (j/execute! tx ["TRUNCATE \"user\" CASCADE"])
          (j/delete! tx :share_assertion [])
          (j/delete! tx :metadata_assertion [])
          (j/delete! tx :sharing_purpose [])
          (j/delete! tx :pii_type [])
          (j/delete! tx :bank []))
        (repl/stop)))))

(use-fixtures :once system-fixture)

;; https://stackoverflow.com/questions/35986244/testing-post-route-with-anti-forgery-and-ring-mock
(defn- get-session
  "Given a response, grab out just the key=value of the ring session"
  [resp]
  (let [cookies         (resp/get-header resp "Set-Cookie")
        session-cookies (first (filter #(.startsWith ^String % "ring-session") cookies))
        session-pair    (first (str/split session-cookies #";"))]
    session-pair))

(defn- get-csrf-field
  "Given an HTML response, parse the body for an anti-forgery input field"
  [resp field]
  (-> (html/select (html/html-snippet (:body resp)) [field])
      first
      (get-in [:attrs :value])))

(use-fixtures :once system-fixture)

(defn- get-session-vars
  [url]
  (let [initial     (*app* (mock/request :get url))
        csrf-token  (get-csrf-field initial :input#__anti-forgery-token)
        session     (get-session initial)]
    (is (= (:status initial) 200))
    (is (not (str/blank? csrf-token)))
    (is (not (str/blank? session)))
    [session csrf-token]))

(defn- register-user
  [username password]
  (let [[session csrf-token]   (get-session-vars "/register")]
    (let [req      (-> (mock/request :post "/register")
                       (assoc :headers {"cookie" session})
                       (assoc :form-params {"__anti-forgery-token" csrf-token
                                            "username" username
                                            "password" password
                                            "password-confirm" password}))
          response (*app* req)]
      (is (= 303 (:status response)))
      (= "/" (resp/find-header response "Location")))
    [session csrf-token]))

(defn- successful-login
  [session csrf-token user-name password]
  (let [req      (-> (mock/request :post "/login")
                     (assoc :headers {"cookie" session})
                     (assoc :form-params {"__anti-forgery-token" csrf-token
                                          "username" user-name
                                          "password" password}))
        response (*app* req)]
    (is (= 302 (:status response)))
    (is (= "http:/" (resp/get-header response "Location")))))

(defn sign-item
  [^PrivateSigningKey signing-key item]
  (let [signature      (.sign signing-key item)
        signature-info (Signed/signed item)]
    (Sequence. [signature signature-info])))

(defn sign-assertion
  [assertion signing-key]
  (let [action-item    (Action. assertion)]
    (sign-item signing-key action-item)))

(def ^PrivateSigningKey priv-key (PrivateSigningKey/generate))

(deftest ^:integration test-app

  (testing "Index route"
    (let [response (*app* (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "health check route"
    (let [response (*app* (mock/request :get "/api/healthcheck"))]
      (is (= (:status response) 200))
      (is (= (json/parse-string (slurp (:body response)))
             {"status" "ok"}))))

  (testing "not-found route"
    (let [response (*app* (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(defn- insert-user-association!
  [processor-id user-name bank-user-id]
  (with-transaction (:db repl/system) [tx]
    (let [bank (store/get-bank-by-name (:db repl/system) tx processor-id)
          user (store/get-user-by-name (:db repl/system) tx user-name false)]
      (j/insert! tx :user_association {:user_id (:id user)
                                       :bank_id (:id bank)
                                       :bank_user_id bank-user-id}))))

(defn- get-signed-assertion
  [priv-key assertion]
  (let [signed-md-assertion (sign-assertion assertion priv-key)
        signed-bytes        (ConvertUtils/toBytes signed-md-assertion)]
    (base64encode signed-bytes)))

(defn- get-assertions
  [session]
  (let [req       (-> (mock/request :get "/assertions")
                      (assoc :headers {"cookie" session}))
        response  (*app* req)]
    (is (:status response) 200)
    (json/parse-string (slurp (:body response)) true)))

(deftest ^:integration test-assertions
  (let [processor-id     "sample_bank"
        subject          "bank-user"
        attribute        "b"
        location         "c"
        pii-type         (PiiType. "pii-type" "Demo PII Type")
        sharing-purpose  (SharingPurpose. (.toString (UUID/randomUUID)) "Demo Sharing Purpose")
        now              (.withNano (time/utc-now) 0)
        created          (time/to-rfc3339-string (time/localdatettime-to-zoned now))
        start-date       (time/to-rfc3339-string (time/localdatettime-to-zoned (.plusDays now -10)))
        end-date         (time/to-rfc3339-string (time/localdatettime-to-zoned (.plusMonths now 3)))]
    (testing "Register key"
      (let [key-bytes (ConvertUtils/toBytes (.getPublicKey priv-key))
            req       (-> (mock/request
                           :post
                           "/api/registerkey")
                          (assoc :form-params
                                 {"bank_name" processor-id
                                  "signing_key" (base64encode key-bytes)
                                  "consus_user" "test-agent@test.labshift.io"
                                  "agent_url" "http://foo.bar:1234"
                                  "oauth_client_id" "test-bank-client"}))
            response (*app* req)]
        (is (= (:status response) 200))))

    (testing "Upload PII Type"
      (let [req      (-> (mock/request
                          :post
                          "/api/piitype")
                         (assoc :form-params
                                {"bank_name" processor-id
                                 "pii_type" (get-signed-assertion priv-key pii-type)}))
            response (*app* req)]
        (is (.contains [409 201] (:status response))
            (format "Got %d as status" (:status response))))) ; 409 is if we've already made this

    (testing "Upload Sharing Purpose"
      (let [req       (-> (mock/request
                           :post
                           "/api/sharingpurpose")
                          (assoc :form-params
                                 {"bank_name" processor-id
                                  "sharing_purpose" (get-signed-assertion priv-key sharing-purpose)}))
            response  (*app* req)]
        (is (= (:status response) 201))))

    (testing "Upload metadata and share assertion"
      (let [md-assertion (MetadataAssertion. (.toString (UUID/randomUUID)) subject (.-id pii-type) processor-id location created)
            req          (-> (mock/request
                              :post
                              "/api/metadataassertion")
                             (assoc :form-params
                                    {"bank_name" processor-id
                                     "assertion" (get-signed-assertion priv-key md-assertion)}))
            response1    (*app* req)]

        (is (= (:status response1) 201))

        (let [s-assertion    (ShareAssertion. processor-id (.-id md-assertion) attribute (.-id sharing-purpose)
                                              created start-date end-date)
              req            (-> (mock/request
                                  :post
                                  "/api/shareassertion")
                                 (assoc :form-params
                                        {"bank_name" processor-id
                                         "assertion" (get-signed-assertion priv-key s-assertion)}))
              response2      (*app* req)]
          (is (= (:status response2) 201)))))

    (testing "Getting assertions as a bank"
      (let [[session csrf-token]  (get-session-vars "/login")
            _                     (successful-login session csrf-token processor-id "fixme!")
            raw-bank-result       (get-assertions session)]

        (testing "Getting assertions as a user"
          (let [user-name             "bob"
                password              "baz"
                [session csrf-token]  (register-user user-name password)
                _                     (insert-user-association! processor-id user-name subject)
                raw-result            (get-assertions session)
                result                (-> raw-result
                                          (update :metadataAssertions (fn [xs]
                                                                        (map (fn [x]
                                                                               (select-keys x [:name :subject
                                                                                               :location :created]))
                                                                             xs)))
                                          (update :shareAssertions (fn [xs]
                                                                     (map (fn [x]
                                                                            (select-keys x [:name :purpose
                                                                                            :created :start_date
                                                                                            :end_date]))
                                                                          xs))))]
            (is (= raw-bank-result raw-result))
            (is (= result
                   {:metadataAssertions [{:name processor-id
                                          :subject subject
                                          :location location
                                          :created created}]
                    :shareAssertions [{:name processor-id
                                       :purpose (.-description sharing-purpose)
                                       :created created
                                       :start_date start-date
                                       :end_date end-date}]}))

            (is (= (get-in raw-result [:metadataAssertions 0 :piitype])
                   (.-description pii-type)))

            (is (= (get-in raw-result [:metadataAssertions 0 :id])
                   (get-in raw-result [:shareAssertions 0 :metadata_assertion_id])))

            (testing "Retracting Share assertion"
              (let [req          (-> (mock/request :post "/shareassertion/revoke")
                                     (assoc :headers {"cookie" session})
                                     (assoc :form-params {"__anti-forgery-token" csrf-token
                                                          "assertion" (get-in raw-result [:shareAssertions 0 :id])
                                                          "validFrom" (time/to-rfc3339-string
                                                                       (time/localdatettime-to-zoned (.plusDays now -1)))}))
                    response     (*app* req)]
                (is (= 201 (:status response)))
                (let [body (json/parse-string (slurp (:body response)) true)]
                  (is (string? (:id body)))
                  (is (uuid? (UUID/fromString (:id body)))))))

            (testing "Try retracting share assertion"
              (let [req          (-> (mock/request :post "/shareassertion/revoke")
                                     (assoc :headers {"cookie" session})
                                     (assoc :form-params {"__anti-forgery-token" csrf-token
                                                          "assertion" (get-in raw-result [:shareAssertions 0 :id])
                                                          "validFrom" (time/to-rfc3339-string
                                                                       (time/localdatettime-to-zoned (.plusDays now -1)))}))
                    response     (*app* req)]
                (is (= 409 (:status response)))
                (let [body (json/parse-string (slurp (:body response)) true)]
                  (is (string? (:id body)))
                  (is (uuid? (UUID/fromString (:id body)))))))

            (testing "Getting assertions again after revoking share assertion"
              (let [assertions (get-assertions session)]
                (is (= 1 (count (:metadataAssertions assertions))))
                (is (= 0 (count (:shareAssertions assertions))))))

            (testing "Retracting Metadata assertion"
              (let [req          (-> (mock/request :post "/metadataassertion/revoke")
                                     (assoc :headers {"cookie" session})
                                     (assoc :form-params {"__anti-forgery-token" csrf-token
                                                          "assertion" (get-in raw-result [:metadataAssertions 0 :id])
                                                          "validFrom" (time/to-rfc3339-string
                                                                       (time/localdatettime-to-zoned (.plusDays now -1)))}))
                    response     (*app* req)]
                (is (= 201 (:status response)))
                (let [body (json/parse-string (slurp (:body response)) true)]
                  (is (string? (:id body)))
                  (is (uuid? (UUID/fromString (:id body)))))))

            (testing "Try retracting metadata assertion again"
              (let [req          (-> (mock/request :post "/metadataassertion/revoke")
                                     (assoc :headers {"cookie" session})
                                     (assoc :form-params {"__anti-forgery-token" csrf-token
                                                          "assertion" (get-in raw-result [:metadataAssertions 0 :id])
                                                          "validFrom" (time/to-rfc3339-string
                                                                       (time/localdatettime-to-zoned (.plusDays now -1)))}))
                    response     (*app* req)]
                (is (= 409 (:status response)))
                (let [body (json/parse-string (slurp (:body response)) true)]
                  (is (string? (:id body)))
                  (is (uuid? (UUID/fromString (:id body)))))))

            (testing "Getting assertions again after revoking metadata assertion"
              (let [assertions (get-assertions session)]
                (is (= 0 (count (:metadataAssertions assertions))))
                (is (= 0 (count (:shareAssertions assertions))))))))))))

(deftest ^:integration testing-register-logging-in
  (testing "Registering"
    (let [user-name             "alice"
          password              "baz"
          [session csrf-token]  (register-user user-name password)]

      (testing "Unsuccessful registration due to mismatching password"
        (let [req      (-> (mock/request :post "/register")
                           (assoc :headers {"cookie" session})
                           (assoc :form-params {"__anti-forgery-token" csrf-token
                                                "username" user-name
                                                "password" password
                                                "password-confirm" "bazz"}))
              response (*app* req)]
          (is (= 400 (:status response)))))

      (testing "Unsuccessful registration due to missing password-confirm"
        (let [req      (-> (mock/request :post "/register")
                           (assoc :headers {"cookie" session})
                           (assoc :form-params {"__anti-forgery-token" csrf-token
                                                "username" user-name
                                                "password" password}))
              response (*app* req)]
          (is (= 400 (:status response)))))

      (testing "Logging in"
        (successful-login session csrf-token user-name password))

      (testing "Getting user info"
        (let [req       (-> (mock/request :get "/userinfo")
                            (assoc :headers {"cookie" session}))
              response  (*app* req)]
          (is (= 200 (:status response)))
          (is (json/parse-string (slurp (:body response)))
              {:userName user-name
               :userType "user"
               :banks []})))

      (testing "Pinging the secure ping api endpoint"
        (let [req       (-> (mock/request :get "/ping")
                            (assoc :headers {"cookie" session})
                            (assoc :query-params {"query" "hello"}))
              response  (*app* req)]
          (is (= 200 (:status response)))
          (is (= (json/parse-string (slurp (:body response)))
                 {"result" "hello"}))))

      (testing "Logging out"
        (let [req       (-> (mock/request :get "/logout")
                            (assoc :headers {"cookie" session}))
              response  (*app* req)]
          (is (= 302 (:status response)))
          (is (= "http:/" (resp/get-header response "Location")))))

      (testing "Hitting index again"
        (let [response  (*app* (mock/request :get "/"))
              session2  (get-session response)]
          (is (= 200 (:status response)))
          (is (not (str/blank? session)))
          (is (not= session2 session))

          (testing "Pinging secure ping api endpoint"
            (let [req       (-> (mock/request :get "/ping")
                                (assoc :headers {"cookie" session2})
                                (assoc :query-params {"query" "hello"}))
                  response2 (*app* req)]
              (is (= 302 (:status response2))))))))))
