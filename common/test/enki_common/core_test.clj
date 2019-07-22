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
(ns enki-common.core-test
  (:require [clojure.test :refer :all])
  (:import [net.lshift.enki MetadataAssertion ShareAssertion]
           [net.lshift.spki.convert ConverterCatalog ConvertUtils]
           [java.time Instant ZonedDateTime ZoneOffset]))

(deftest metadata-assertions
  (testing "Should parse dates using RFC3339 when needed"
    (are [date-str expected]
         (let [sample (MetadataAssertion. "enkiId" "subjectId" "piiType" "processorId" "http://foo.com/" date-str)]
           (= (.getCreatedAt sample) expected))
      "1985-04-12T23:20:50.52Z"
      (Instant/from (ZonedDateTime/of 1985 4 12 23 20 50 520000000 ZoneOffset/UTC))
      "1996-12-19T16:39:57-08:00"
      (Instant/from (ZonedDateTime/of 1996 12 19 16 39 57 0 (ZoneOffset/ofHours -8))))))

(deftest share-assertions
  (testing "Should parse dates using RFC3339 when needed"
    (let [example (ShareAssertion.
                   "enkiId" "metadataId" "sharingProcessorId" "purposeId"
                   "1985-04-12T23:20:50.52Z"
                   "1996-12-19T16:39:57-08:00"
                   "1996-12-20T16:39:57-08:00")]
      (is (= (.getCreatedAt example) (Instant/from (ZonedDateTime/of 1985 4 12 23 20 50 520000000 ZoneOffset/UTC))))
      (is (= (.getConsentStart example) (Instant/from (ZonedDateTime/of 1996 12 19 16 39 57 0 (ZoneOffset/ofHours -8)))))
      (is (= (.getConsentEnd example) (Instant/from (ZonedDateTime/of 1996 12 20 16 39 57 0 (ZoneOffset/ofHours -8))))))))

(deftest test-conversions
  (testing "MetadataAssertion should be convertable for Bletchley"
    (let [m  (MetadataAssertion. "enkiId" "subjectId" "piiType" "processorId" "http://foo.com/" "1985-04-12T23:20:50Z")
          m' ^MetadataAssertion (ConvertUtils/fromBytes ConverterCatalog/BASE (class m) (ConvertUtils/toBytes m))]
      (is (= (.-subjectId m) (.-subjectId m')))
      (is (= (.-location m) (.-location m')))
      (is (= (.-createdAt m) (.-createdAt m')))))

  (testing "ShareAssertion should be convertable for Bletchley"
    (let [m (ShareAssertion.
             "enkiId" "metadataId" "sharingProcessorId" "purposeId"
             "1985-04-12T23:20:50Z"
             "1996-12-19T16:39:57-08:00"
             "1996-12-20T16:39:57-08:00")
          m' ^ShareAssertion (ConvertUtils/fromBytes ConverterCatalog/BASE (class m) (ConvertUtils/toBytes m))]
      (is (= (.-purposeId m) (.-purposeId m')))
      (is (= (.-createdAt m) (.-createdAt m')))
      (is (= (.-consentStart m) (.-consentStart m')))
      (is (= (.-consentEnd m) (.-consentEnd m'))))))
