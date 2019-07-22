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
(ns enki-core.store.core-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.java.jdbc :as j]
            [com.stuartsierra.component :as component]
            [enki-core.store.core :as store]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.migrator :as migrator]
            [enki-core.store.sql :as sql-store]
            [environ.core :refer [env]]
            [reloaded.repl :as repl]))

(def ^:dynamic *tx*)

(defn test-system
  [_]
  (component/system-map
   :db (sql-store/instance (env :database-url))
   :migrator (component/using
              (migrator/instance)
              [:db])))

(defn system-fixture
  [f]
  (repl/set-init! #(test-system {}))
  (repl/go)
  (try
    (f)
    (finally
      (with-transaction (:db repl/system) [tx]
        (j/execute! tx ["TRUNCATE \"user\" CASCADE"]))
      (repl/stop))))

(defn transaction-fixture
  [f]
  (with-transaction (:db repl/system) [tx]
    (binding [*tx* tx]
      (f)
      (j/db-set-rollback-only! *tx*))))

(use-fixtures :once system-fixture)
(use-fixtures :each transaction-fixture)

(deftest ^:integration test-inserting-user
  (testing "Inserting a user"
    (let [user     {:name "FooInsertTest" :password "Bar"}
          response (store/insert-user! (:db repl/system) *tx* (:name user) (:password user))]
      (is (uuid? (:id response)))
      (is (= (:name user) (:name response)))
      (is (store/check-password? (:db repl/system) *tx* (:name user) (:password user)))
      (is (not (store/check-password? (:db repl/system) *tx* (:name user) "Baz")))
      (is (thrown? Exception (store/check-password? (:db repl/system) *tx* "FooInsertTest2" "Baz"))))))
