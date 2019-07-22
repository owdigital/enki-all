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
(ns enki-core.web.secure-api
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET POST]]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.time :as time]
            [ring.util.http-response :as hresp]
            [clj-http.client :as client])
  (:import (java.util UUID)))

(defn ping [query-params]
  (hresp/ok {:result (get query-params "query")}))

(defn userinfo [db session]
  (with-transaction db [tx {:read-only? true}]
    (let [user      (store/get-user-by-name db tx (:identity session) false)
          bank?     (store/is-user-bank? db tx (:id user))
          user-type (if bank?
                      "bank"
                      "user")
          associations (store/get-user-associations db tx (:id user))
          services     (store/get-services-for-user db tx (:id user))]
      (hresp/ok {:userName (:name user)
                 :userType user-type
                 :banks (map :bank_id associations)
                 :services services}))))

(defn assertions [db session]
  (with-transaction db [tx {:read-only? true}]
    (let [user  (store/get-user-by-name db tx (:identity session) false)
          now   (time/utc-now)
          bank? (store/is-user-bank? db tx (:id user))]
      (if bank?
        (hresp/ok {:metadataAssertions (store/get-bank-metadata-assertions db tx (:id user) now)
                   :shareAssertions (store/get-bank-share-assertions db tx (:id user) now)})
        (hresp/ok {:metadataAssertions (store/get-metadata-assertions db tx (:id user) now)
                   :shareAssertions (store/get-share-assertions db tx (:id user) now)})))))

(defn revoke-share [db session form-params]
  (with-transaction db [tx]
    (let [user        (store/get-user-by-name db tx (:identity session) false)
          now         (time/utc-now)
          assertion   (UUID/fromString (get form-params "assertion"))
          valid-from  (.toLocalDateTime (time/from-rfc3339-string (get form-params "validFrom")))
          existing    (store/get-revoked-share-assertion db tx assertion)]
      (if existing
        {:status 409
         :headers {}
         :body existing}
        {:status 201
         :headers {}
         :body (store/revoke-share-assertion! db tx (:id user) assertion valid-from now)}))))

(defn revoke-metadata [db session form-params]
  (with-transaction db [tx]
    (let [user        (store/get-user-by-name db tx (:identity session) false)
          now         (time/utc-now)
          assertion   (UUID/fromString (get form-params "assertion"))
          valid-from  (.toLocalDateTime (time/from-rfc3339-string (get form-params "validFrom")))
          existing    (store/get-revoked-metadata-assertion db tx assertion)]
      (if existing
        {:status 409
         :headers {}
         :body existing}
        {:status 201
         :headers {}
         :body (store/revoke-metadata-assertion! db tx (:id user) assertion valid-from now)}))))

(defn add-service [db session form-params]
  (with-transaction db [tx]
    (try
      (let [user        (store/get-user-by-name db tx (:identity session) false)
            services    (store/get-services-for-user db tx (:id user))
            service-id  (UUID/fromString (:id form-params))
            proof-url   (:proof_url form-params)]
        (if (not-any? #{service-id} services)
          {:status 201
           :headers {}
           :body (store/add-service! db tx (:id user) service-id proof-url)}
          {:status 201
           :headers {}
           :body "already added"}))
      (catch Exception e (log/info "something wrong" (.getMessage e) (.printStackTrace e))))))

(defn secure-api-routes
  [{:keys [db] :as components}]
  (routes
   (GET "/ping" {:keys [query-params]} (ping query-params))
   (GET "/userinfo" {:keys [session]} (userinfo db session))
   (GET "/assertions" {:keys [session]} (assertions db session))
   (POST "/service/add" {:keys [session body-params] :as request} (add-service db session body-params))
   (POST "/shareassertion/revoke" {:keys [session form-params]} (revoke-share db session form-params))
   (POST "/metadataassertion/revoke" {:keys [session form-params]} (revoke-metadata db session form-params))))

