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
(ns enki-agent.command.core
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [enki-agent.systems :as systems]
            [environ.core :refer [env]]))

(defmulti run-command (fn [args] (first args)))

(defmethod run-command :default
  [args]
  (log/errorf "Unknown command: %s" (first args)))

(defmethod run-command "agent"
  [args]
  (let [[_ key-file] args
        {:keys [enki-server-url port consus-user consus-config bank-name agent-url oauth-client-id]
         :or {port "3010"
              consus-user "test-agent@test.labshift.io"
              consus-config "/root/upspin"
              bank-name "sample-bank"
              oauth-client-id "bank-client"}} env
        config #:enki-agent.systems{:enki-server-url enki-server-url
                                    :enki-key-file key-file
                                    :port (Integer/parseInt port)
                                    :consus-user consus-user
                                    :consus-config consus-config
                                    :bank-name bank-name
                                    :agent-url agent-url
                                    :oauth-client-id oauth-client-id}]
    (log/info ::using-config config)
    (if (s/valid? systems/config-spec config)
      (let [sys (-> (systems/system config)
                    component/start-system)]
        (log/infof "running")
        (systems/run-until-done sys))
      (throw (ex-info "Could not validate configuration inputs"
                      {:reason (s/explain-str systems/config-spec config)})))))
