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
(ns enki-agent.command.new-signing-key
  "Generates a new private signing key and sends the public one to enki-core"
  (:require [clojure.java.io :as io]
            [enki-agent.command.core :as core]
            [enki-agent.bletchley.signer :as signer]
            [enki-agent.enki-service :as enki]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])
  (:import (net.lshift.spki.convert ConvertUtils)
           (java.util Base64)))

(defn generate-and-send-key
  "Generates a private signing key, sends the public key to enki-core, then saves private key to disk"
  [enki-server filename]
  (let [key        (signer/generate-signing-key)
        key-bytes  (-> key .getPublicKey ConvertUtils/toBytes)]
    (enki/register-key enki-server key-bytes)
    (signer/write-object-file filename key)))

(defmethod core/run-command "new-signing-key"
  [args]
  (let [{:keys [enki-server-url bank-name consus-user agent-url oauth-client-id]} env]
    (assert enki-server-url "ENKI_SERVER_URL environment variable is not set")
    (assert bank-name "BANK_NAME environment variable is not set")
    (assert consus-user "CONSUS_USER environment variable is not set")
    (assert agent-url "AGENT_URL environment variable is not set")
    (assert oauth-client-id "OAUTH_CLIENT_ID environment variable is not set")
    (log/infof "Connecting to %s with %s, %s, %s, %s" enki-server-url 
      bank-name consus-user agent-url oauth-client-id)
    (generate-and-send-key (enki/make enki-server-url bank-name consus-user agent-url oauth-client-id)
                           (nth args 1))))
