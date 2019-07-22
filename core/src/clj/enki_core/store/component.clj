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
(ns enki-core.store.component
  (:require [com.stuartsierra.component :as component]
            [enki-core.store.core :as s-core]
            [enki-core.store.migrator :as migrator]
            [enki-core.store.sql :as sql]))

(defn get-database-components
  [{:keys [:enki-core.system/database-url]}]
  (component/system-map
   :db (sql/instance database-url)
   :migrator (component/using
              (migrator/instance)
              [:db])))

(defmacro with-transaction
  [db binding & body]
  `(s-core/-run-transaction!
    ~db
    (^{:once true} fn* [~(first binding)] ~@body)
    ~@(rest (rest binding))))
