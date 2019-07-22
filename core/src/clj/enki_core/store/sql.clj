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
(ns enki-core.store.sql
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [enki-core.store.core :as store]
            [hikari-cp.core :as hc]
            [hugsql.core :as hugsql]
            [clojure.tools.logging :as log])
  (:import
   (java.sql Date Timestamp)
   (java.util UUID)))

(hugsql/def-db-fns "sql/healthcheck.sql")
(hugsql/def-db-fns "sql/user.sql")
(hugsql/def-db-fns "sql/assertion.sql")

;; https://github.com/clj-time/clj-time/blob/master/src/clj_time/jdbc.clj
;; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/IResultSetReadColumn

(extend-protocol jdbc/IResultSetReadColumn
  Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime ^Timestamp v))
  Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate ^Date v)))

(defrecord PostgresStore [jdbc-url datasource]
  component/Lifecycle

  (start [component]
    (log/info ::starting jdbc-url)
    (if datasource
      component
      (assoc component :datasource (hc/make-datasource {:jdbc-url jdbc-url}))))

  (stop [component]
    (when datasource
      (log/info ::closing jdbc-url)
      (hc/close-datasource datasource))
    (assoc component :datasource nil))

  store/DataStore

  (-run-transaction! [component func]
    (store/-run-transaction! component func {}))

  (-run-transaction! [_ func opts]
    (jdbc/db-transaction* {:datasource datasource} func opts))

  (healthcheck [_ tx]
    (let [res (-query-healthcheck
               tx)]
      (= (:result res) 1)))

  (user-exists? [_ tx user-name]
    (:exists
     (-check-user-name
      tx
      {:name user-name})))

  (create-user! [_ tx user-name password]
    (-create-user!
     tx
     {:name user-name
      :password password}))

  (update-password! [_ tx id password]
    (-update-user-password!
     tx
     {:id id
      :password password}))

  (get-user [_ tx id]
    (-get-user
     tx
     {:id id}))

  (get-user-by-name [_ tx user-name password?]
    (if password?
      (-get-user-by-name-password
       tx
       {:name user-name})
      (-get-user-by-name
       tx
       {:name user-name})))

  (get-banks [_ tx]
    (-get-banks tx))

  (get-bank-by-id [_ tx bank-id]
    (-get-bank-by-id
     tx
     {:id bank-id}))

  (get-bank-by-name [_ tx bank-name]
    (-get-bank-by-name
     tx
     {:name bank-name}))

  (get-bank-by-oauth-client [_ tx oauth-client-id]
    (-get-bank-by-oauth-client
     tx
     {:oauth-client-id oauth-client-id}))

  (insert-bank! [_ tx user-id k consus-user agent-url oauth-client-id]
    (-insert-bank!
     tx
     {:user user-id
      :pub-key k
      :consus-user consus-user
      :agent-url agent-url
      :oauth-client-id oauth-client-id}))

  (is-user-bank? [_ tx user-id]
    (:exists
     (-is-user-bank
      tx
      {:user user-id})))

  (list-services [_ tx]
    (-list-services tx))

  (get-services-for-user [_ tx user-id]
    (-get-services-for-user
     tx
     {:user-id ^UUID user-id}))

  (get-pii-type [_ tx id]
    (-get-pii-type tx {:id id}))

  (insert-pii-type! [_ tx id description]
    (-insert-pii-type! tx {:id id :description description}))

  (get-purpose [_ tx id]
    (-get-purpose tx {:id id}))

  (insert-purpose! [_ tx id description]
    (-insert-purpose! tx {:id id :description description}))

  (insert-metadata-assertion! [_ tx bank data]
    (let [data-with-id (if (contains? data :id) data (assoc data :id (UUID/randomUUID)))]
      (-insert-metadata-assertion!
       tx
       (assoc data-with-id
              :bank_id
              bank))))

  (get-metadata-assertion [_ tx metadata-id now]
    (-get-metadata-assertion
     tx
     {:id ^UUID metadata-id
      :now now}))

  (get-metadata-assertions [_ tx user-id now]
    (-get-metadata-assertions
     tx
     {:user user-id
      :now now}))

  (get-bank-metadata-assertions [_ tx bank-user-id now]
    (-get-bank-metadata-assertions
     tx
     {:user bank-user-id
      :now now}))

  (insert-share-assertion! [_ tx bank data]
    (-insert-share-assertion!
     tx
     (assoc data
            :sharing_bank_id
            bank)))

  (get-share-assertions [_ tx user-id now]
    (-get-share-assertions
     tx
     {:user user-id
      :now now}))

  (get-bank-share-assertions [_ tx bank-user-id now]
    (-get-bank-share-assertions
     tx
     {:user bank-user-id
      :now now}))

  (insert-user-association! [_ tx user-id bank-id bank-user-id]
    (-insert-user-association!
     tx
     {:user-id user-id
      :bank-id bank-id
      :bank-user-id bank-user-id}))

  (get-user-association [_ tx user-id bank-id]
    (-get-user-association
     tx
     {:user-id user-id
      :bank-id bank-id}))

  (get-user-associations [_ tx user-id]
    (-get-user-associations
     tx
     {:user-id user-id}))

  (revoke-metadata-assertion! [db tx user-id assertion-id valid-from now]
    ;; Add share assertion revocations linked with the metadata assert
    (doseq [{id :id}  (-get-metadata-linked-share-assertions tx {:id assertion-id})]
      (when-not (store/get-revoked-share-assertion db tx id)
        (store/revoke-share-assertion! db tx user-id id valid-from now)))

    ;; Add metadata assertion revocation
    (-create-metadata-assertion-revocation
     tx
     {:user user-id
      :valid-from valid-from
      :assertion assertion-id
      :now now}))

  (revoke-share-assertion! [_ tx user-id assertion-id valid-from now]
    (-create-share-assertion-revocation
     tx
     {:user user-id
      :valid-from valid-from
      :assertion assertion-id
      :now now}))

  (get-revoked-share-assertion [_ tx assertion-id]
    (-get-share-revocation
     tx
     {:id assertion-id}))

  (get-revoked-metadata-assertion [_ tx assertion-id]
    (-get-metadata-revocation
     tx
     {:id assertion-id}))

  (get-metadata-assertions-given-pii-types [_ tx user-id pii-types now]
    (-get-metadata-assertions-given-pii-types
     tx
     {:now now
      :user user-id
      :pii-types pii-types}))

  (get-assertions-locations [_ tx ids now]
    (-get-metadata-assertion-locations
     tx
     {:now now
      :ids ids}))

  (add-service! [_ tx user-id service-id proof-url]
    (-add-service
     tx
     {:user-id ^UUID user-id
      :service-id ^UUID service-id
      :proof-url proof-url})))

(defn instance
  [jdbc-url]
  (map->PostgresStore {:jdbc-url jdbc-url}))
