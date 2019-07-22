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
(ns enki-core.api
  (:require [compojure.api.sweet :refer [api context routes GET POST]]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [enki-core.hydra.consent :as hconsent]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.time :as time]
            [ring.util.response :as resp]
            [ring.util.http-response :as hresp]
            [spec-tools.spec :as spec])
  (:import (java.util Base64 UUID)
           (net.lshift.enki MetadataAssertion PiiType ShareAssertion SharingPurpose)
           (net.lshift.spki.suiteb PublicSigningKey Sequence InferenceEngine ActionType)
           (net.lshift.spki.convert ConvertUtils ConverterCatalog)
           (net.lshift.spki.suiteb.simplemessage SimpleMessage)))

(def catalog (.extend
              ConverterCatalog/BASE
              (into-array (class ActionType) [SimpleMessage MetadataAssertion PiiType ShareAssertion SharingPurpose])))

(defn- base64decode
  [^String data-str]
  (let [decoder (Base64/getDecoder)]
    (.decode decoder data-str)))

(defn- read-trusted-assertion
  "Read the signed assertion bytes and extract an assertion object
   if the signature is verified by the public signing key provided"
  [bank-signing-key assertion-bytes clazz]
  (let [assertion-message   (ConvertUtils/fromBytes catalog Sequence assertion-bytes)
        engine              (doto (InferenceEngine. catalog)
                              (.processTrusted bank-signing-key)
                              (.process assertion-message))]
    (.getSoleAction engine clazz)))

(defn- store-pii-type
  [db tx ^PiiType pii-type]
  (store/insert-pii-type! db tx (.-id pii-type) (.-description pii-type)))

(defn- store-metadata-assertion
  [db tx bank-id ^MetadataAssertion assertion signature]
  (let [data {:id (UUID/fromString (.-id assertion))
              :subject (.-subjectId assertion)
              :pii_type (.-piiType assertion)
              :location (str (.-location assertion))
              :created (time/instant->utc-datetime (.getCreatedAt assertion))
              :signature signature}]
    (store/insert-metadata-assertion! db tx bank-id data)))

(defn- store-purpose
  [db tx ^SharingPurpose purpose]
  (store/insert-purpose! db tx (UUID/fromString (.-id purpose)) (.-description purpose)))

(defn- store-share-assertion
  [db tx bank-id ^ShareAssertion assertion signature]
  (let [data {:metadata-assertion-id (UUID/fromString (.-metadataId assertion))
              :purpose (UUID/fromString (.-purposeId assertion))
              :created (time/instant->utc-datetime (.getCreatedAt assertion))
              :consent-start (time/instant->utc-datetime (.getConsentStart assertion))
              :consent-end (time/instant->utc-datetime (.getConsentEnd assertion))
              :signature signature}]
    (store/insert-share-assertion! db tx bank-id data)))

(defn- handle-assertion
  [assertion-encoded bank-name db tx klass]
  (let [assertion-bytes   (base64decode assertion-encoded)
        bank              (store/get-bank-by-name db tx bank-name)
        bank-signing-key  (->> (get bank :pub_key)
                               base64decode
                               (ConvertUtils/fromBytes catalog PublicSigningKey))
        bank-id           (get bank :id)
        assertion         (read-trusted-assertion bank-signing-key assertion-bytes klass)]
    [assertion-encoded bank-id assertion]))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::location string?)
(s/def ::pii string?)
(s/def ::description string?)
(s/def ::status string?)
(s/def ::bank-name string?)
(s/def ::signing-key string?)
(s/def ::consus-user string?)
(s/def ::agent-url string?)
(s/def ::service-url string?)
(s/def ::oauth-client-id string?)
(s/def ::assertion-encoded string?)
(s/def ::pii-type-name string?)
(s/def ::pii-type (s/keys :req-un [::id ::description]))
(s/def ::pii-location (s/keys :req-un [::id ::location ::pii]))
(s/def ::pii-locations (s/* ::pii-location))
(s/def ::authorization string?)
(s/def ::bank (s/keys :req-un [::id ::name]))
(s/def ::banks (s/* ::bank))
(s/def ::service (s/keys :req-un [::id ::name ::service-url]))
(s/def ::services (s/* ::service))

(defn- extract-token-from-auth-header
  [auth-header]
  (let [[_ id-token] (re-matches #"^Bearer (.*)$" auth-header)]
    id-token))

(defn- get-share-locations
  [{:keys [hydra-token]} db auth-header]
  (let [auth-res    (hconsent/validate-token hydra-token
                                             (extract-token-from-auth-header auth-header))
        now         (time/utc-now)
        ids         (->> (str/split (:scope auth-res) #" ")
                         (map (fn [x]
                                (try
                                  (UUID/fromString x)
                                  (catch IllegalArgumentException _))))
                         (filter identity))]
    (with-transaction db [tx {:read-only? true}]
      (store/get-assertions-locations db tx ids now))))

(defn- inner-api-routes
  [{:keys [db] :as components}]
  (routes
   (GET "/healthcheck" []
     :summary "Healthcheck"
     :description "Returns database healthcheck status"
     :return (s/keys :req-un [::status])
     (with-transaction db [tx {:read-only? true}]
       (if (store/healthcheck db tx)
         (hresp/ok {:status "ok"})
         (hresp/service-unavailable! {:status "error"}))))

   (GET "/banks" {}
     :summary "Returns a list of data processors known to core"
     :return ::banks
     (with-transaction db [tx {:read-only? true}]
       (let [banks (store/get-banks db tx)]
         (hresp/ok (map (fn [x]
                          {:id (str (:id x))
                           :name (:name x)})
                        banks)))))

   (GET "/services" {}
     :summary "Returns a list of social ID services known to core"
     :return ::services
     (with-transaction db [tx {:read-only? true}]
       (let [services (store/list-services db tx)]
         (hresp/ok (map #(select-keys % '(:id :name :url)) services)))))

   (POST "/registerkey" []
     :summary "Receive bank's public signing key and store in db"
     :return (s/keys :req-un [::status])
     :form-params [signing_key :- ::signing-key
                   bank_name :- ::bank-name
                   consus_user :- ::consus-user
                   agent_url :- ::agent-url
                   oauth_client_id :- ::oauth-client-id]
     (with-transaction db [tx]
       (let [key-data-encoded signing_key]
        ;; TODO: This endpoint now expects the bank to send password for creating
        ;; a user account. By default we are setting it to \"fixme!\"

        ;; Verify we have what we expect
        ;; This will throw an exception if the data isn't in the right shape
         (ConvertUtils/fromBytes catalog PublicSigningKey (base64decode key-data-encoded))
         (log/infof "Signing key received for %s" bank_name)
         (if (store/get-user-by-name db tx bank_name false)
           (-> (resp/response {:status "Error" :message (format "Conflict with existing user called %s" bank_name)}) (resp/status 409))
           (let [user (store/insert-user! db tx bank_name "fixme!")
                 bank (store/insert-bank! db tx (:id user) key-data-encoded consus_user agent_url oauth_client_id)]
             (resp/response {:status "OK"
                             :result bank}))))))

   (GET "/piitype/:id" []
     :summary "Returns a description of a PII Type"
     :path-params [id :- ::pii-type-name]
     :return ::pii-type
     (with-transaction db [tx]
       (let [pii-type (store/get-pii-type db tx id)]
         {:status 200
          :headers {}
          :body pii-type})))

   (POST "/piitype" []
     :summary "Receive new PII Type and store in db"
     :form-params [pii_type :- ::assertion-encoded
                   bank_name :- ::bank-name]
     :return spec/int?
     (with-transaction db [tx]
       (let [[_ _ ^PiiType pii-type] (handle-assertion pii_type bank_name db tx PiiType)
             existing                (store/get-pii-type db tx (.-id pii-type))
             obj                     (if existing
                                       existing
                                       (store-pii-type db tx pii-type))]
         (if existing
           (do
             (log/info "Already have PII type" (.-id pii-type))
             {:status 409
              :headers {}
              :body existing})
           (do
             (log/info "Stored PII type" (.-id pii-type))
             {:status 201
              :headers {}
              :body obj})))))

   (POST "/metadataassertion" []
     :form-params [assertion :- ::assertion-encoded
                   bank_name :- ::bank-name]
     :return spec/int?
     :summary "Receive the metadata assertion, verify the signature and store the assertion and signed data in db"
     (with-transaction db [tx]
       (let [[assertion-encoded bank-id assertion] (handle-assertion assertion bank_name db tx MetadataAssertion)
             obj                                   (store-metadata-assertion db tx bank-id assertion assertion-encoded)]
         (log/info "Successfully extracted metadata assertion")
         {:status 201
          :headers {}
          :body obj})))

   (POST "/sharingpurpose" []
     :summary "Receive new Sharing Purpose and store in db"
     :form-params [sharing_purpose :- ::assertion-encoded
                   bank_name :- ::bank-name]
     :return spec/int?
     (with-transaction db [tx]
       (let [[_ _ ^SharingPurpose sharing-purpose] (handle-assertion sharing_purpose bank_name db tx SharingPurpose)
             existing                              (store/get-purpose db tx (UUID/fromString (.-id sharing-purpose)))
             obj                                   (if existing
                                                     existing
                                                     (store-purpose db tx sharing-purpose))]
         (if existing
           (do
             (log/info "Already have Sharing Purpose" (.-id sharing-purpose))
             {:status 409
              :headers {}
              :body existing})
           (do
             (log/info "Stored Sharing Purpose" (.-id sharing-purpose))
             {:status 201
              :headers {}
              :body obj})))))

   (POST "/shareassertion" []
     :form-params [assertion :- ::assertion-encoded
                   bank_name :- ::bank-name]
     :return spec/int?
     :summary "Receive the share assertion, verify the signature and store the assertion and signed data in db"
     (with-transaction db [tx]
       (let [[assertion-encoded bank-id assertion] (handle-assertion assertion bank_name db tx ShareAssertion)
             obj                                   (store-share-assertion db tx bank-id assertion assertion-encoded)]
         (log/info "Successfully extracted share assertion")
         {:status 201
          :headers {}
          :body obj})))

   (GET "/sharelocations" []
     :header-params [authorization :- ::authorization]
     :return ::pii-locations
     :summary "Returns locations of PII data for which the user can given consent given a valid OAuth2 bearer token."
     {:status 201
      :headers {}
      :body (get-share-locations components db authorization)})))

(defn api-routes
  [components]
  (api
   {:swagger
    {:ui "/api-docs"
     :spec "/swagger.json"
     :data {:info {:title "Enki API"
                   :description ""
                   :version "0.1"}
            :tags [{:name "api" :description ""}]}}}
   (context "/api" []
     :tags ["api"]
     :coercion :spec
     (inner-api-routes components))))
