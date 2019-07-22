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
(ns enki-agent.http-smoke-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.stuartsierra.component :as component]
            [enki-agent.systems :as sys]
            [enki-agent.test-junk-drawer :refer [tempfile]]
            [environ.core :refer [env]]
            [clj-http.client :as client]
            [ring.util.response :as resp]
            [clojure.spec.test.alpha :as stest]
            [enki-agent.bletchley.signer :as signer])
  (:import (net.lshift.spki.suiteb PrivateSigningKey Sequence Signed Action)
           (net.lshift.spki.convert ConvertUtils)
           (java.io File)
           (java.util Base64)
           (net.lshift.enki MetadataAssertion ShareAssertion)))

(stest/instrument)

(def ^:dynamic *url*)
(def key-file (tempfile "sign" "key"))

(def test-config
  #:enki-agent.systems{;; We do not currently make requests to this. If we need to; we should create a stub.
                       :enki-server-url "http://localhost:3000/"
                       :enki-key-file (.getAbsolutePath ^File key-file)
                       :port 0
                       :consus-user "foo"
                       :consus-config "fnt"
                       :bank-name "bar"
                       :oauth-client-id "bar-client"})

(defn test-system []
  (sys/system test-config))

(defn- make-sign-key []
  (signer/write-object-file
   key-file
   (signer/generate-signing-key)))

(defn system-fixture [f]
  (make-sign-key)
  (let [sys  (-> (test-system) component/start-system)
        port (-> sys :enki-agent.systems/http :port)]
    (binding [*url* (format "http://localhost:%d/" port)]
      (try
        (f)
        (finally
          (component/stop-system sys))))))

(use-fixtures :once system-fixture)

(deftest ^:integration test-app
  (testing "health check route"
    (let [response (client/get (str *url* "/healthcheck"))]
      (is (= (:status response) 200)))))
