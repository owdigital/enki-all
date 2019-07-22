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
(ns enki-core.store.sql-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.java.jdbc :as j]
            [com.stuartsierra.component :as component]
            [enki-core.store.core :as store]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.migrator :as migrator]
            [enki-core.store.sql :as sql-store]
            [environ.core :refer [env]]
            [reloaded.repl :as repl])
  (:import (java.time LocalDateTime)
           (java.util UUID)))

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
        (j/execute! tx ["TRUNCATE \"user\" CASCADE"])
        (j/delete! tx :share_assertion [])
        (j/delete! tx :metadata_assertion [])
        (j/delete! tx :bank [])
        (j/delete! tx :sharing_purpose [])
        (j/delete! tx :pii_type []))
      (repl/stop))))

(defn transaction-fixture
  [f]
  (with-transaction (:db repl/system) [tx]
    (binding [*tx* tx]
      (store/insert-pii-type! (:db repl/system) *tx* "dummy" "Dummy PII Type")
      (f)
      (j/db-set-rollback-only! *tx*))))

(use-fixtures :once system-fixture)

(use-fixtures :each transaction-fixture)

(defn- duplicate-user-data
  [purpose]
  (let [user     {:name "bank-2" :password "Bar"}
        res-user (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
        bank     (store/insert-bank! (:db repl/system) *tx* (:id res-user) "sample-key" "foo@bar.com" "http://bar.url:1234" "sample-client")
        md       (store/insert-metadata-assertion! (:db repl/system)
                                                   *tx*
                                                   (:id bank)
                                                   {:subject "user-1"
                                                    :pii_type "dummy"
                                                    :location "in your house again"
                                                    :created (LocalDateTime/of 2017 1 1 9 0)
                                                    :signature "signature"})
        sa       (store/insert-share-assertion! (:db repl/system)
                                                *tx*
                                                (:id bank)
                                                {:bank_id "user-1"
                                                 :metadata-assertion-id (:id md)
                                                 :purpose (:id purpose)
                                                 :created (LocalDateTime/of 2017 1 1 9 1)
                                                 :consent-start (LocalDateTime/of 2017 1 2 0 0)
                                                 :consent-end (LocalDateTime/of 2017 1 3 0 0)
                                                 :signature "signature"})]
    (store/insert-user-association! (:db repl/system) *tx* (:id res-user) (:id bank) "user-1")
    (is (uuid? (:id md)))
    (is (uuid? (:id sa)))
    (let [[mas :as metadata-assertions] (store/get-metadata-assertions (:db repl/system) *tx* (:id res-user) (LocalDateTime/of 2017 1 2 1 0))
          [sas :as share-assertions]    (store/get-share-assertions (:db repl/system) *tx* (:id res-user) (LocalDateTime/of 2017 1 2 1 0))]
      (is (= 1 (count metadata-assertions)))
      (is (= (:name user) (:name mas)))
      (is (= (:subject mas) "user-1"))
      (is (= 1 (count share-assertions)))
      (is (= (:name user) (:name sas))))))

(deftest ^:integration test-sql-store
  (testing "Adding a user"
    (let [user     {:name "Foo" :password "Bar"}
          response (store/create-user! (:db repl/system) *tx* (:name user) (:password user))]
      (is (uuid? (:id response)))
      (is (= (select-keys user [:name])
             (select-keys response [:name])))))

  (testing "Checking if username exists"
    (let [user     {:name "Foo11" :password "Bar"}]
      (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
      (is (store/user-exists? (:db repl/system) *tx* (:name user)))
      (is (not (store/user-exists? (:db repl/system) *tx* "Foo12345xv")))))

  (testing "Updating a password"
    (let [user      {:name "Foo2" :password "Bar"}
          user2     (assoc user :password "Baz")
          response  (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
          response2 (store/update-password! (:db repl/system) *tx* (:id response) (:password user2))]
      (is (= (:id response) (:id response2)))
      (is (= (select-keys user2 [:name])
             (select-keys response2 [:name])))))

  (testing "Getting a user"
    (let [user      {:name "Foo3" :password "Bar"}
          response  (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
          response2 (store/get-user (:db repl/system) *tx* (:id response))
          response3 (store/get-user-by-name (:db repl/system) *tx* (:name response) false)]
      (is (uuid? (:id response2)))
      (is (= (select-keys user [:name])
             (select-keys response2 [:name])))
      (is (= response2 response3))))

  (testing "Inserting and getting a bank"
    (let [user      {:name "sample-bank-123" :password "Bar"}
          res-user  (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
          response  (store/insert-bank! (:db repl/system) *tx* (:id res-user) "sample-key" "foo@bar.com" "http://bar.url:1234" "sample-client")]
      (is (uuid? (:id response)))
      (is (store/is-user-bank? (:db repl/system) *tx* (:id res-user)))
      (is (= {:name "sample-bank-123"
              :pub_key "sample-key"}
             (select-keys (store/get-bank-by-name (:db repl/system) *tx* "sample-bank-123")
                          [:name :pub_key])))
      (is (= [{:name "sample-bank-123"}]
             (map #(select-keys % [:name]) (store/get-banks (:db repl/system) *tx*))))))

  (testing "Inserting metadata and share assertions"
    (let [bank-name "sample-bank-1234"
          user      {:name bank-name :password "Bar"}
          res-user  (store/create-user! (:db repl/system) *tx* (:name user) (:password user))
          bank      (store/insert-bank! (:db repl/system) *tx* (:id res-user) "sample-key" "foo@bar.com" "http://bar.url:1234" "sample-client")
          md        (store/insert-metadata-assertion! (:db repl/system)
                                                      *tx*
                                                      (:id bank)
                                                      {:subject "user-1"
                                                       :pii_type "dummy"
                                                       :location "in your house"
                                                       :created (LocalDateTime/of 2017 1 1 9 0)
                                                       :signature "signature"})
          _         (store/insert-user-association! (:db repl/system) *tx* (:id res-user) (:id bank) "user-1")
          md'       (store/get-metadata-assertion (:db repl/system) *tx* (:id md) (LocalDateTime/of 2018 1 1 1 0))
          purpose   (store/insert-purpose! (:db repl/system) *tx* (UUID/randomUUID) "Dummy Sharing Purpose")
          sa        (store/insert-share-assertion! (:db repl/system)
                                                   *tx*
                                                   (:id bank)
                                                   {:bank_id "user-1"
                                                    :metadata-assertion-id (:id md)
                                                    :purpose (:id purpose)
                                                    :created (LocalDateTime/of 2017 1 1 9 1)
                                                    :consent-start (LocalDateTime/of 2017 1 2 0 0)
                                                    :consent-end (LocalDateTime/of 2017 1 3 0 0)
                                                    :signature "signature"})
          md-pii    (store/get-metadata-assertions-given-pii-types (:db repl/system) *tx* (:id res-user) ["dummy"] (LocalDateTime/of 2018 1 1 1 0))]
      (is (uuid? (:id bank)))
      (is (uuid? (:id md)))
      (is (= (:id md)
             (:id md')))
      (is (uuid? (:id sa)))
      (is (= 1 (count md-pii)))
      (is (= (:id (first md-pii))
             (:id md)))
      (is (= bank-name (:bank_name (first md-pii))))
      (is (= "dummy" (:pii (first md-pii))))
      (is (= "Dummy PII Type" (:description (first md-pii))))
      (let [[mas :as metadata-assertions] (store/get-metadata-assertions (:db repl/system) *tx* (:id res-user) (LocalDateTime/of 2017 1 2 1 0))
            [sas :as share-assertions]    (store/get-share-assertions (:db repl/system) *tx* (:id res-user) (LocalDateTime/of 2017 1 2 1 0))]
        (is (= 1 (count metadata-assertions)))
        (is (= (:name mas) bank-name))
        (is (= (:subject mas) "user-1"))
        (is (= 1 (count share-assertions)))
        (is (= (:name sas) bank-name)))
      (duplicate-user-data purpose))))
