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
(ns enki-agent.command.sign
  "Provides functions to read a json assertion from file and send it to enki-core
   if it conforms to schema. The assertion is signed with bank's private key.
   Before doing this the bank must send their public key to enki-core."
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [enki-agent.bletchley.signer :as signer]
            [enki-agent.endpoints :refer [endpoints]]
            [enki-agent.enki-service :as enki]
            [enki-agent.http-server :as http-server]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]])
  (:import (java.util UUID)
           (net.lshift.spki.suiteb Sequence SequenceItem PrivateSigningKey InferenceEngine)
           (net.lshift.spki.suiteb.simplemessage SimpleMessage)
           (net.lshift.spki.convert ConverterCatalog ConvertUtils)
           (net.lshift.spki.convert.openable OpenableUtils FileOpenable)
           (net.lshift.enki MetadataAssertion PiiType ShareAssertion SharingPurpose)))

(s/def ::component
  (s/keys :req-un [::signing-key-file ::signing-key]))

(defrecord AssertionSigner [signing-key-file signing-key]
  component/Lifecycle

  (start [{:keys [signing-key-file] :as component}]
    (log/infof "Start AssertionSigner; load key %s", signing-key-file)
    (let [key (signer/read-object-file signing-key-file ConverterCatalog/BASE PrivateSigningKey)]
      (assert key (format "signing key from %s" signing-key-file))
      (assoc component :signing-key key)))

  (stop [component]
    (log/infof "Stop AssertionSigner")
    (assoc component :signing-key nil)))

(defn- construct-metadata-assertion
  [assertion-json]
  (let [{:strs [^String id ^String subjectId ^String piiType ^String location ^String processorId ^String createdAt]} assertion-json]
    (MetadataAssertion. id subjectId piiType processorId location createdAt)))

(defn- construct-share-assertion
  [assertion-json]
  (let [{:strs [^String id ^String metadataId ^String sharingProcessorId ^String purposeId ^String createdAt ^String consentStart ^String consentEnd]} assertion-json]
    (ShareAssertion. id metadataId sharingProcessorId purposeId createdAt consentStart consentEnd)))

(defn- construct-pii-type
  [assertion-json]
  (let [{:strs [^String id ^String description]} assertion-json]
    (PiiType. id description)))

(defn- construct-sharing-purpose
  [assertion-json]
  (let [{:strs [^String id ^String description]} assertion-json]
    (SharingPurpose. id description)))

(defn- construct-assertion
  [assertion-json]
  (let [{:strs [type]} assertion-json]
    (case type
      "metadata-assertion" (construct-metadata-assertion assertion-json)
      "share-assertion"    (construct-share-assertion assertion-json)
      "pii-type"           (construct-pii-type assertion-json)
      "sharing-purpose"    (construct-sharing-purpose assertion-json))))

(defn- get-endpoint
  [assertion-type]
  (case assertion-type
    "metadata-assertion" (:metadata endpoints)
    "share-assertion"    (:share endpoints)
    "pii-type"           (:pii-type endpoints)
    "sharing-purpose"    (:sharing-purpose endpoints)))

(defn sign-assertion
  [signing-key assertion-json]
  (let [assertion-type   (get assertion-json "type")
        assertion        (construct-assertion assertion-json)
        signed-assertion (signer/sign-assertion assertion signing-key)
        signed-bytes     (ConvertUtils/toBytes signed-assertion)
        endpoint         (get-endpoint assertion-type)]
    {:signed-bytes signed-bytes
     :endpoint endpoint}))

