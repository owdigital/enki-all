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
(ns enki-core.web.secure
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [compojure.core :refer [routes GET POST]]
            [enki-core.agent :as agent]
            [enki-core.hydra.consent :as consent]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.time :as time]
            [enki-core.web.core :as web]))

; FIXME: Magic number. See resources/db/migration/V20171025105600__DefaultData.sql
; In the future, we should be handed it by something
(def purpose-id "a4cd2c37-786d-4601-a0b5-83e32b1e0077")

(s/def ::challenge string?)
(s/def ::consent-query (s/keys :req-un [::challenge]))

(defn- map-bank-ids [db tx ids]
  (let [banks (map #(store/get-bank-by-id db tx %) ids)
        users (map #(hash-map (:id %) {:consus (:consus_user %) :agent (:agent_url %)}) banks)]
    (apply merge users)))

(defn- manage-agent-data
  [db tx decoded-challenge assertion-data]
  (let [metadata-info      (vals assertion-data)
        banks              (->> metadata-info
                                (map :bank_id)
                                distinct
                                (map-bank-ids db tx))
        oauth-client       (:aud decoded-challenge)
        bank               (store/get-bank-by-oauth-client db tx oauth-client)]
    (log/infof "Bank: %s" (pr-str bank))
    (agent/allow-metadata-access metadata-info (:consus_user bank) banks)
    (dorun (map #(agent/make-share-assertion bank (:id %) purpose-id) metadata-info))))

(defn- get-consent-assertion-data
  [db tx session form-params decoded-challenge]
  (let [now                (time/utc-now)
        user               (store/get-user-by-name db tx (:identity session) false)
        ;; Group by `pii` and then create a dict where key is `id` for the groups
        scope-data         (->> (store/get-metadata-assertions-given-pii-types db
                                                                               tx
                                                                               (:id user)
                                                                               (:scp decoded-challenge)
                                                                               now)
                                (group-by :pii)
                                (map (fn [[k v]]
                                       [k
                                        (into {}
                                              (map (fn [x]
                                                     [(str (:id x))
                                                      {:id (str (:id x))
                                                       :location (:location x)
                                                       :bank_id (:bank_id x)}])
                                                   v))]))
                                (into {}))]
    ;; Check if there is an overlap between scopes being asked and what the user agreed to and
    ;; if those are also present in scope-data.
    (->> (:scp decoded-challenge)
         (map
          (fn [x]
            (let [y (get form-params x)]
              (when (and (not (str/blank? y))
                         (contains? (get scope-data x) y))
                [x (get-in scope-data [x y])]))))
         (filter (fn [[_ v]]
                   v))
         (into {}))))

(defn- post-consent
  [components db tx query-params form-params session]
  (let [decoded-challenge  (consent/verify-consent-challenge (:hydra-token components)
                                                             (get query-params "challenge"))
        assertion-data     (get-consent-assertion-data db tx session form-params decoded-challenge)
        accepted-scope-ids (->> assertion-data
                                vals
                                (map :id))]
    (manage-agent-data db tx decoded-challenge assertion-data)
    (log/infof "Decoded challenge: %s" (pr-str decoded-challenge))
    (log/infof "Accepted Scope IDs: %s" (pr-str accepted-scope-ids))
    (consent/resolve-consent (:hydra-token components)
                             decoded-challenge
                             (set/union (set accepted-scope-ids)
                                        #{"openid" "offline" "hydra.clients"})
                             (store/get-user-by-name db tx (:identity session) false)
                             {})))

(defn secure-routes
  [{:keys [db] :as components}]
  (routes
   (GET "/consent" {:keys [params session] :as request}
     (if (not (s/valid? ::consent-query params))
       (web/get-html-response (s/explain-str ::consent-query params) 400)
       (let [now               (time/utc-now)
             challenge         (get params :challenge)
             decoded-challenge (consent/verify-consent-challenge
                                (:hydra-token components)
                                challenge)
             client-name       (-> (:aud decoded-challenge)
                                   (str/replace #"-client" "")
                                   (str/replace #"-" " ")
                                   str/upper-case)
             scopes            (disj (set (:scp decoded-challenge))
                                     "openid"
                                     "offline"
                                     "hydra.clients")
             scope-data        (with-transaction db [tx {:read-only? true}]
                                 (let [user (store/get-user-by-name db tx (:identity session) false)]
                                   (store/get-metadata-assertions-given-pii-types db tx (:id user) scopes now)))
             scope-data        (map (fn [x]
                                      (assoc x :bank_css_id (-> (:bank_name x)
                                                                str/lower-case
                                                                (str/replace #" " "-"))))
                                    scope-data)]
         (web/render-file
          components
          "consent/consent"
          {:scopes (map (fn [[k v]]
                          {:id k
                           :banks v
                           :description (:description (first v))})
                        (group-by :pii scope-data))
           :client-name client-name
           :client-id (:aud decoded-challenge)
           :challenge challenge}))))

   (POST "/consent" {:keys [query-params session form-params]}
     (with-transaction db [tx {:read-only? true}]
       (post-consent components db tx query-params form-params session)))))
