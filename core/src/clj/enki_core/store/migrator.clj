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
(ns enki-core.store.migrator
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log])
  (:import (org.flywaydb.core Flyway)))

(defrecord Migrator [db]

  component/Lifecycle

  (start [component]
    (log/info ::migrating-with db)
    (let [ds   (:datasource db)]
      (assert ds "DB must provide datasource")
      (log/info ::ds ds)
      (doto (Flyway.)
        (.setDataSource ds)
        (.migrate)))
    component)

  (stop [component]
    component))

(defn instance
  []
  (map->Migrator {}))
