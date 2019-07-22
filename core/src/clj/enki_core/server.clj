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
(ns enki-core.server
  (:require [aleph.http :as http]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ring.logger :as logger])
  (:import (java.io Closeable)))

(defrecord WebServer [port server handler]
  component/Lifecycle

  (start [component]
    (log/infof "Starting web server on http://localhost:%s" port)
    (if server
      component
      (assoc component
             :server (http/start-server (logger/wrap-with-logger (:app handler)) {:port port}))))

  (stop [component]
    (log/info "Stopping webserver")
    (when server
      (.close ^Closeable server))
    (assoc component :server nil)))

(defn instance
  [port]
  (map->WebServer {:port port}))
