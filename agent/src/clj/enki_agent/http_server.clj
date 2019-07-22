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
(ns enki-agent.http-server
  (:require [aleph.http :as http]
            [aleph.netty :as netty]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [ring.logger :as logger])
  (:import (java.io Closeable)))

(defprotocol HttpHandler
  (get-handler [component]))

(s/def ::component
  (s/keys :req-un [::port ::app]
          :opt-un [::server]))
(defrecord WebServer [port app server]
  component/Lifecycle

  (start [{:keys [app server] :as component}]
    (if server
      component
      (let [server (http/start-server (logger/wrap-with-logger (get-handler app)) {:port port})
            port (netty/port server)]
        (log/infof "Starting web server on http://0.0.0.0:%d/" port)
        (assoc component :server server :port port))))

  (stop [{:keys [server] :as component}]
    (log/info "Stopping webserver")
    (when server
      (.close ^Closeable server))
    (assoc component :server nil)))

(defn instance
  [port]
  (map->WebServer {:port port}))

(s/fdef run-until-done :args ::component)
(defn run-until-done [{:keys [:server]}]
  (netty/wait-for-close server))
