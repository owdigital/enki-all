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
(ns enki-agent.command.new-signing-key-test
  (:require [clojure.test :refer [deftest testing is]]
            [enki-agent.bletchley.signer :as signer]
            [enki-agent.command.new-signing-key :as new-key]
            [enki-agent.test-junk-drawer :refer :all]
            [enki-agent.enki-service :as srv]
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(deftest command-wrapper
  (testing "Sends a some data to a service"
    (let [sent-data (atom nil)
          fake-server (reify srv/EnkiService
                        (register-key [this data]
                          (reset! sent-data data)))
          key-file   (tempfile "new" "key")]

      (new-key/generate-and-send-key fake-server  key-file)
      (is (instance? (type (byte-array [])) @sent-data))
      (is (< 0 (-> key-file io/as-file .length))))))
