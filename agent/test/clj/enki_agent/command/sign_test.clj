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
(ns enki-agent.command.sign-test
  (:require
   [enki-agent.bletchley.signer :as signer]
   [enki-agent.command.sign :as command-sign]
   [enki-agent.test-junk-drawer :refer :all]
   [enki-agent.enki-service :as srv]
   [enki-agent.http-server :as http-server]
   [enki-agent.routing :as routing]

   [clojure.test :refer [deftest testing is]]
   [clojure.tools.logging :as log]
   [cheshire.core :as json]
   [ring.mock.request :as mock]
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]))

(def signing-key
  (delay
   (let [t (tempfile "sign" "key")
         private-signing-key (signer/generate-signing-key)]
     (signer/write-object-file t private-signing-key)
     t)))

;; TODO: Add fuzzing to ensure we validate the input.
(deftest http-service
  (let [sent-data (atom nil)
        fake-server (reify srv/EnkiService
                      (send-signed-assertion [this type data endpoint]
                        (log/infof "received data %s to %s" data endpoint)
                        (reset! sent-data [endpoint data])))
        signer (command-sign/map->AssertionSigner {:signing-key-file @signing-key})
        router (routing/map->Router {})
        sys (component/system-map
             :enki-server fake-server
             :signer signer
             :routing (component/using router [:enki-server :signer]))
        {:keys [routing]} (component/start-system sys)
        handler (http-server/get-handler routing)]

    (testing "healthcheck"
      (let [response (handler (mock/request :get "/healthcheck"))]
        (is (= (:status response) 200))))

    (testing "Sends a byte array to metadata service"
      (reset! sent-data nil)
      (let [request (->
                     (mock/request :post "/data" (slurp metadata-assertion-file))
                     (mock/content-type "application/json"))
            response (handler request)]
        (is (= (:status response) 200))
        (let [[endpoint data] @sent-data]
          (is (= endpoint "/api/metadataassertion"))
          (is (instance? (type (byte-array [])) data) response))))

    (testing "Sends a byte array to share assertion endpoint"
      (reset! sent-data nil)
      (let [request (->
                     (mock/request :post "/data" (slurp share-assertion-file))
                     (mock/content-type "application/json"))
            response (handler request)]
        (is (= (:status response) 200) response)
        (let [[endpoint data] @sent-data]
          (is (= endpoint "/api/shareassertion"))
          (is (instance? (type (byte-array [])) data)))))))
