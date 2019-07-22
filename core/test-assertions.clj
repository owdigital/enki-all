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
(require '[clojure.java.jdbc :as j])
(require '[enki-core.store.component :refer [with-transaction]])
(require '[enki-core.store.core :as store])
(require '[clj-http.client :as client])
(require '[reloaded.repl :as repl])
(require '[slingshot.slingshot :refer [try+]])
(require '[clojure.tools.logging :as log])

(import '(net.lshift.spki.suiteb PrivateSigningKey Sequence Signed Action)
        '(net.lshift.spki.convert ConvertUtils)
        '(java.util Base64 UUID)
        '(net.lshift.enki MetadataAssertion ShareAssertion))

(defn- base64encode
  [data]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder data)))

(defn- sign-item
  [^PrivateSigningKey signing-key item]
  (let [signature      (.sign signing-key item)
        signature-info (Signed/signed item)]
    (Sequence. [signature signature-info])))

(defn- sign-assertion
  [assertion signing-key]
  (let [action-item    (Action. assertion)]
    (sign-item signing-key action-item)))

(defn- insert-user!
  [user-name password]
  (with-transaction (:db repl/system) [tx]
    (store/insert-user! (:db repl/system) tx user-name password)))

(defn- insert-user-association!
  [bank-name user-name bank-user-id]
  (with-transaction (:db repl/system) [tx]
    (let [bank (store/get-bank-by-name (:db repl/system) tx bank-name)
          user (store/get-user-by-name (:db repl/system) tx user-name false)]
      (j/insert! tx :user_association {:user_id (:id user)
                                       :bank_id (:id bank)
                                       :bank_user_id bank-user-id}))))

(defonce user-name "alicia")

(defonce ^PrivateSigningKey priv-key-a (PrivateSigningKey/generate))
(defonce ^String bank-name-a "bank-a")
(defonce ^String bank-user-a "alicia")

(defonce ^PrivateSigningKey priv-key-b (PrivateSigningKey/generate))
(defonce ^String bank-name-b "bank-b")

(defonce ^String m-id (str (UUID/randomUUID)))
(defonce ^String s-id (str (UUID/randomUUID)))

(defonce ^String m-id2 (str (UUID/randomUUID)))
(defonce ^String s-id2 (str (UUID/randomUUID)))

(defonce ^String s-id3 (str (UUID/randomUUID)))

;; Bank A is registered by the Bank Agent
(comment
  (try+
   (client/post "http://localhost:3000/api/registerkey"
                {:form-params {"bank_name" bank-name-a
                               "signing_key" (base64encode (ConvertUtils/toBytes (.getPublicKey priv-key-a)))
                               "consus_user" "test-agent@test.labshift.io"
                               "agent_url" "http://agent:3010"
                               "oauth_client_id" "bank-a-client"}})
   (catch [:status 409] _
     (log/warn (format "Already added %s" bank-name-a)))))

(try+
 (client/post "http://localhost:3000/api/registerkey"
              {:form-params {"bank_name" bank-name-b
                             "signing_key" (base64encode (ConvertUtils/toBytes (.getPublicKey priv-key-b)))
                             "consus_user" "test-agent2@test.labshift.io"
                             "agent_url" "http://agent2:3010"
                             "oauth_client_id" "bank-b-client"}})
 (catch [:status 409] _
   (log/warn (format "Already added %s" bank-name-b))))

(let [test-pii (client/post "http://agent:3010/data"
                            {:form-params {:type "pii-data"
                                           :id (str (UUID/randomUUID))
                                           :piiType "firstName"
                                           :subjectId bank-user-a
                                           :processorId bank-name-a
                                           :value "foo"}
                             :content-type :json
                             :as :json})]
  (client/post "http://agent:3010/data"
               {:form-params {:type "metadata-assertion"
                              :processorId "1"
                              :id m-id
                              :piiType "firstName"
                              :subjectId bank-user-a
                              :location (-> test-pii :body :location)
                              :createdAt "2017-01-01T13:52:43Z"}
                :content-type :json
                :as :json}))

(client/post "http://agent:3010/data"
             {:form-params {:type "share-assertion"
                            :id s-id
                            :metadataId m-id
                            :sharingProcessorId bank-name-a
                            :purposeId "a4cd2c37-786d-4601-a0b5-83e32b1e0077"
                            :createdAt "2017-01-01T13:52:43Z"
                            :consentStart "2017-01-01T13:52:43Z"
                            :consentEnd "2018-01-01T13:52:43Z"}
              :content-type :json
              :as :json})

(let [test-pii (client/post "http://agent:3010/data"
                            {:form-params {:type "pii-data"
                                           :id (str (UUID/randomUUID))
                                           :piiType "lastName"
                                           :subjectId bank-user-a
                                           :processorId bank-name-a
                                           :value "bar"}
                             :content-type :json
                             :as :json})]
  (client/post "http://agent:3010/data"
               {:form-params {:type "metadata-assertion"
                              :processorId "1"
                              :id m-id2
                              :piiType "lastName"
                              :subjectId bank-user-a
                              :location (-> test-pii :body :location)
                              :createdAt "2017-01-01T13:52:43Z"}
                :content-type :json
                :as :json}))

(client/post "http://agent:3010/data"
             {:form-params {:type "share-assertion"
                            :id s-id2
                            :metadataId m-id2
                            :sharingProcessorId bank-name-a
                            :purposeId "a4cd2c37-786d-4601-a0b5-83e32b1e0077"
                            :createdAt "2017-01-01T13:52:43Z"
                            :consentStart "2017-01-01T13:52:43Z"
                            :consentEnd "2018-01-01T13:52:43Z"}
              :content-type :json
              :as :json})

(client/post "http://localhost:3000/api/shareassertion"
             {:form-params {"bank_name" bank-name-b
                            "assertion" (base64encode
                                         (ConvertUtils/toBytes
                                          (sign-assertion
                                           (ShareAssertion. s-id3 m-id2 bank-name-b "a4cd2c37-786d-4601-a0b5-83e32b1e0077" "2017-01-01T13:52:43Z" "2017-01-01T13:52:43Z" "2018-01-01T13:52:43Z") priv-key-b)))}})

(insert-user! user-name "123")

;; This actually links the enki and bank accounts.
;; (insert-user-association! bank-name-a user-name bank-user-a)
