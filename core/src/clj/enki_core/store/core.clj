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
(ns enki-core.store.core
  (:require [buddy.hashers :as hashers])
  (:import
   (java.util UUID)))

(defprotocol DataStore
  ;; Core methods
  (-run-transaction! [_ func] [_ func opts] "Run the function inside a transaction if the store supports it. Please use `enki-core.store.component/with-transaction` macro.")
  (healthcheck [this tx] "Returns true if we are able to access the store, false otherwise")

  ;; Users
  (user-exists? [this tx user-name] "Returns true if user with the given `user-name` exists, false otherwise")
  (create-user! [this tx user-name password] "Creates a user with given `user-name` and `password`. Returns a map with the two properties and an additional `id` property.")
  (update-password! [this tx id new-password] "Updates `password` for a given user `id` returning user")
  (get-user [this tx id] "Returns a user with the given `id`")
  (get-user-by-name [this tx user-name password?] "Returns a user with optional hashed password with the given `name`")
  (is-user-bank? [this tx user-id] "Returns true if user is a bank")

  ;; Banks
  (get-banks [this tx] "Gets banks")
  (get-bank-by-id [this tx bank-id] "Gets a bank given an ID")
  (get-bank-by-name [this tx bank-name] "Gets a bank given a name")
  (get-bank-by-oauth-client [this tx oauth-client-id] "Gets a bank given an OAuth client name")
  (insert-bank! [this tx user-id pub-key consus-user agent-url oauth-client-id] "Create a bank given a user id, public key, consus user, agent url and OAuth client id")

  ;; Services
  (list-services [this tx] "Get services")
  (get-services-for-user [this tx user-id] "Get services for a user")

  ;; PII Types
  (get-pii-type [this tx id] "Get PII Type by id")
  (insert-pii-type! [this tx id description] "Insert PII Type")

  ;; Sharing Purposes
  (get-purpose [this tx id] "Get Sharing Purpose by id")
  (insert-purpose! [this tx id description] "Insert Sharing Purpose")

  ;; Assertions
  (insert-metadata-assertion! [this tx ^UUID bank-id data] "Insert a metadata assertion a bank-id and a metadata assertion")
  (get-metadata-assertion [this tx ^UUID metadata-id now] "Get a metadata assertion given metadata Id")
  (get-metadata-assertions [this tx ^UUID user-id now] "Get metadata assertions given user-id")
  (insert-share-assertion! [this tx ^UUID bank-id data] "Insert a share assertion a bank-id and a share assertion")
  (get-share-assertions [this tx ^UUID user-id now] "Get share assertions given user-id")
  (get-bank-metadata-assertions [this tx ^UUID bank-user-id now] "Get all metadata assertions given bank-user-id")
  (get-bank-share-assertions [this tx ^UUID bank-user-id now] "Get all share assertions given bank-user-id")

  (get-metadata-assertions-given-pii-types [this tx ^UUID user-id pii-types now] "Get metadata assertions given pii-types given a user-id")
  (get-assertions-locations [this tx metadata-ids now] "Gets locations of metadata assertions given a list of IDs")

  ;; Association
  (insert-user-association! [_ tx user-id bank-id bank-user-id] "Insert a user association with bank user id")
  (get-user-association [this tx user-id bank-id] "Returns a user association with the given `user-id` and `bank-id`")
  (get-user-associations [this tx user-id] "Returns user associations with the given `user-id`")

  (revoke-metadata-assertion! [this tx ^UUID user-id ^UUID assertion-id valid-from now])
  (revoke-share-assertion! [this tx ^UUID user-id ^UUID assertion-id valid-from now])
  (get-revoked-share-assertion [this tx ^UUID assertion-id])
  (get-revoked-metadata-assertion [this tx ^UUID assertion-id])

  (add-service! [this tx ^UUID user-id ^UUID service-id proof-url]))

(defn insert-user!
  [db tx user-name password]
  ;; TODO: Upgrade to using Argon2 Password Hasher
  ;; https://github.com/funcool/buddy-hashers/issues/13
  ;; https://en.wikipedia.org/wiki/Argon2
  (create-user! db tx user-name (hashers/derive password)))

(defn check-password?
  [db tx user-name attempt]
  (let [user (get-user-by-name db tx user-name true)]
    (when (nil? user)
      (throw (ex-info "Unable to find user"
                      {:user-name user-name})))
    (hashers/check attempt (:password user))))
