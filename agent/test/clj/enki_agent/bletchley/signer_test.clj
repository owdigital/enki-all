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
(ns enki-agent.bletchley.signer-test
  (:require [clojure.test :refer [deftest testing is]]
            [enki-agent.bletchley.signer :as signer]
            [enki-agent.command.sign :as command-sign]
            [enki-agent.enki-service :as srv]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (net.lshift.spki.suiteb.simplemessage SimpleMessage)
           (net.lshift.spki.suiteb InferenceEngine Sequence ActionType Action PrivateSigningKey PublicSigningKey)
           (net.lshift.spki.convert ConverterCatalog ConvertUtils)
           (net.lshift.enki MetadataAssertion ShareAssertion)
           (java.util Base64 UUID)
           (java.text ParseException)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)
           (java.time.format DateTimeParseException)))

(def catalog (.extend ConverterCatalog/BASE (into-array
                                             (class ActionType)
                                             [SimpleMessage MetadataAssertion ShareAssertion])))

(defn- base64decode
  [^String data-str]
  (let [decoder (Base64/getDecoder)]
    (.decode decoder data-str)))

(defn- base64encode
  [data]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder data)))

(defn- tempfile [prefix suffix]
  (.toFile (Files/createTempFile prefix suffix (into-array FileAttribute []))))

(defn- process-assertion
  [bank-signing-key assertion-bytes clazz]
  (let [assertion-message      (ConvertUtils/fromBytes catalog Sequence assertion-bytes)
        engine                 (doto (InferenceEngine. catalog)
                                 (.processTrusted bank-signing-key)
                                 (.process assertion-message))]
    (.getSoleAction engine clazz)))

(defn- extract-message [^PublicSigningKey public-key obj-read]
  (let [engine (doto (InferenceEngine. catalog)
                 (.processTrusted public-key)
                 (.process obj-read))]
    (.getSoleAction engine SimpleMessage)))

(deftest signing-test
  (testing "Should produce a signature verifiable by Bletchley"
    (let [filepath       (tempfile "test" "file")
          data           (byte-array [65 66 67])
          item           (Action. (SimpleMessage. "test" data))
          signing-key    (signer/generate-signing-key)
          signed-data    (signer/sign-item signing-key item)
          _              (signer/write-object-file filepath signed-data)
          obj-read       (signer/read-object-file filepath catalog Sequence)
          message        (extract-message (.getPublicKey signing-key) obj-read)
          extracted-data (.content ^SimpleMessage message)]
      (is (= (String. data)
             (String. extracted-data))))))

(def metadata-assertion-file
  (io/resource "enki_agent/bletchley/data/metadata-assertion"))

(def signing-key
  (delay
   (let [t (tempfile "sign" "key")
         private-signing-key (signer/generate-signing-key)]
     (signer/write-object-file t private-signing-key)
     t)))

(defn sign-assertion-file
  "Read an assertion from file (string), constructs one of the assertion class instances from it
   and signs it with the private signing key. Also picks the right enki-core endpoint"
  [^String signing-key-file assertion-file]
  (when (or (nil? signing-key-file)
            (nil? assertion-file))
    (log/error "To sign data pass filenames for signing key and input file")
    (throw (ex-info "Invalid parameters", [signing-key-file assertion-file])))
  (let [assertion-str    (slurp assertion-file)
        assertion-json   (json/parse-string assertion-str)
        signing-key      (signer/read-object-file signing-key-file ConverterCatalog/BASE PrivateSigningKey)]
    (command-sign/sign-assertion signing-key assertion-json)))

(deftest assertion-test
  (testing "Can extract assertion after transmission (for enki-core)"
    (let [signed-data                            (sign-assertion-file @signing-key metadata-assertion-file)
          signed-bytes                           (:signed-bytes signed-data)
          signed-bytes                           (base64decode (base64encode signed-bytes))
          ^PrivateSigningKey private-signing-key (signer/read-object-file @signing-key ConverterCatalog/BASE PrivateSigningKey)
          public-signing-key                     (.getPublicKey private-signing-key)
          ^MetadataAssertion assertion           (process-assertion public-signing-key signed-bytes MetadataAssertion)
          assertion-str                          (slurp metadata-assertion-file)
          assertion-json                         (json/parse-string assertion-str true)]
      (is (= (.-subjectId assertion) (:subjectId assertion-json)))
      (is (= (.-piiType assertion) (:piiType assertion-json)))
      (is (= (-> assertion .-location str) (:location assertion-json)))))

  (testing "Fails to construct assertion with incorrect schema"
    (is (thrown? DateTimeParseException (MetadataAssertion. (.toString (UUID/randomUUID)) "user123" "pii-type" "bank-name" "htps://some-location" "52:43")))))