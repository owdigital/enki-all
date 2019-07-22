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
(ns enki-agent.core
  (:require [clojure.tools.logging :as log]
            [enki-agent.command.core :as command]
            [enki-agent.command.new-signing-key]
            [enki-agent.command.sign])
  (:import (net.lshift.spki.convert ConvertUtils ConverterCatalog)
           (net.lshift.spki.suiteb.simplemessage SimpleMessage)
           (net.lshift.spki.suiteb PrivateSigningKey))
  (:gen-class))

(defn -main
  [& args]
  (when (zero? (count args))
    (log/error "No command given. Valid commands are: agent, new-signing-key")
    (log/error "Exiting...")
    (System/exit -1))
  (command/run-command args))
