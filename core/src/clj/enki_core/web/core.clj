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
(ns enki-core.web.core
  (:require [buddy.auth :refer [authenticated?]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.walk :as walk]
            [enki-core.store.core :as store]
            [enki-core.util :refer [get-banks-from-env]]
            [enki-core.util :refer [get-bank-urls-from-env]]
            [hbs.core :as hbs]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.codec :refer [form-encode]]
            [ring.util.response :as resp])
  (:import (java.util HashMap Map)))

(defn correct-credentials? [db tx {:strs [username password]}]
  (try
    (store/check-password? db tx username password)
    (catch Exception _
      false)))

(defn get-html-response
  ([body] (get-html-response body 200))
  ([body status]
   (-> (resp/response body)
       (resp/status status)
       (update-in [:headers "Content-Type"]
                  (fn [ct]
                    (if (some? ct) ct "text/html; charset=utf-8"))))))

(defn render-file-index
  [components tmpl-name context logged-in? admin?]
  (let [reg  (:registry components)
        body (hbs/render-file reg
                              "layouts/main"
                              (merge
                               {:contextProperties {:csrfToken *anti-forgery-token*
                                                    :loggedIn logged-in?
                                                    :isAdmin admin?
                                                    :banks (get-banks-from-env)
                                                    :currentUser (walk/stringify-keys (:user context))
                                                    :bankUrls (get-bank-urls-from-env)}}
                               (:context components)
                               context
                               {:body (hbs/render-file reg tmpl-name context)}))]
    (get-html-response body)))

(defn render-file
  ([components tmpl-name context] (render-file components tmpl-name context 200))
  ([components tmpl-name context status]
   (let [reg  (:registry components)
         body (hbs/render-file reg
                               tmpl-name
                               (merge
                                (:context components)
                                {:csrfToken *anti-forgery-token*}
                                context))]
     (get-html-response body status))))

;; User defined unauthorized handler
;;
;; This function is responsible for handling
;; unauthorized requests (when unauthorized exception
;; is raised by some handler)

(defn unauthorized-handler
  [components]
  (fn [request metadata]
    (cond
      ;; If request is authenticated, raise 403 instead
      ;; of 401 (because user is authenticated but permission
      ;; denied is raised).
      (authenticated? request)
      (render-file components "error" 403)
      ;; In other cases, redirect the user to login page.
      :else
      (let [current-url  (:uri request)
            query-params (merge {"next" current-url
                                 "error" "Please log in"}
                                (:query-params request))]
        (resp/redirect (str "/login?" (form-encode query-params)))))))

(defn convert-for-handlebars
  [model]
  (walk/postwalk
   #(cond
      (map? %) (HashMap. ^Map %)
      (keyword? %) (name %)
      :else %)
   model))
