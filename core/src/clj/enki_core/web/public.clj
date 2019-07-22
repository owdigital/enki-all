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
(ns enki-core.web.public
  (:require [compojure.core :refer [routes GET POST]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.web.core :as web]
            [enki-core.web.register :refer [register]]
            [ring.util.codec :refer [form-encode]]
            [ring.util.response :as resp]))

(defn app-routes
  [{:keys [db] :as components}]
  (routes
   (GET "/" {:keys [session]}
     (with-transaction db [tx {:read-only? true}]
       (let [user (if (:identity session)
                    (store/get-user-by-name db tx (:identity session) false)
                    {:admin false})
             result {:user user}]
         (web/render-file-index components "index" result (some? (:identity session)) (:admin user)))))

   (GET "/register" {:keys [query-params]}
     (web/render-file components "register" {}))

   (POST "/register" {:keys [session form-params]}
     (register components db session form-params))

   (GET "/login" {:keys [query-params session]}
     (-> (web/render-file components "login" {:error (get query-params "error")
                                              :next (get query-params "next")
                                              :challenge (get query-params "challenge")})
         (assoc :session (dissoc session :identity))))

   (POST "/login" {:keys [form-params query-params session]}
     (with-transaction db [tx {:read-only? true}]
       (let [challenge   (get query-params "challenge")
             next-url    (get query-params "next")
             next-url    (if (str/blank? next-url)
                           "/"
                           next-url)
             user-name   (get form-params "username")
             correct?    (web/correct-credentials? db tx form-params)]
         (cond
           (and correct? (str/blank? challenge))
           (-> (resp/redirect next-url)
               (assoc :session (assoc session :identity user-name)))

           (and correct? (not (str/blank? challenge)))
           (-> (resp/redirect (str "/consent?" (form-encode {"challenge" challenge})))
               (assoc :session (assoc session :identity user-name)))

           :else
           (resp/redirect (str "/login?"
                               (form-encode (merge query-params {"error" "Wrong credentials provided"}))))))))

   (GET "/logout" {:keys [session]}
     (-> (resp/redirect "/")
         (assoc :session nil)))

   (route/resources "/")))
