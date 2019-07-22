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
(ns enki-core.main
  (:require [aleph.netty :refer [wait-for-close]]
            [enki-core.system :refer [instance] :as system]
            [environ.core :refer [env]]
            [hugsql.core :as sql]
            [hugsql.adapter.clojure-java-jdbc :as adp]
            [reloaded.repl :as repl])
  (:gen-class))

(defn -main
  [& args]
  (assert (env :http-port) "HTTP_PORT environment variable is not set")
  (assert (env :database-url) "DATABASE_URL environment variable is not set")

  ;; See: https://github.com/layerware/hugsql/issues/46#issuecomment-326752037
  (sql/set-adapter! (adp/hugsql-adapter-clojure-java-jdbc))

  (repl/set-init! #(instance {::system/dev? false
                              ::system/http-port (env :http-port)
                              ::system/database-url (env :database-url)}))
  (repl/go)
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable repl/stop))
  (wait-for-close (get-in repl/system [:web-server :server])))
