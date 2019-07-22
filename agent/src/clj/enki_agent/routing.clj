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
(ns enki-agent.routing
  (:require
   [enki-agent.command.sign :as sign]
   [enki-agent.enki-service :as enki]
   [enki-agent.http-server :as http-server]
   [enki-agent.upspin :as upspin]

   [com.stuartsierra.component :as component]
   [compojure.core :refer [routes GET POST wrap-routes]]
   [compojure.route :refer [not-found]]
   [ring.util.response :as resp]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
   [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [clojure.spec.alpha :as s]
   [slingshot.slingshot :refer [try+]]
   [clojure.walk :refer [keywordize-keys]]
   [clojure.data.json :as json]
   [clojure.tools.logging :as log]
   [clojure.string :as string]))

(defn- error-result [msg]
  {:type "error" :error msg})

(defn- send-assertion [{:keys [enki-server signer] :as component} json-params]
  (let [{:keys [endpoint signed-bytes]}
        (sign/sign-assertion (:signing-key signer) json-params)
        type (keyword (get json-params "type"))]
    (try+
     (do
       (enki/send-signed-assertion enki-server type signed-bytes endpoint)
       (resp/response {:type "enki"}))
     (catch #(.contains [400 409] (:status %)) {:keys [body status]}
       (-> (resp/response (error-result (json/read-str body))) (resp/status status))))))

(s/fdef send-data-subject
        :args (s/cat
               :component (s/keys :req-un [::consus-user ::consus-config])
               :obj (s/keys :req-un [::id ::type ::subjectType ::processorId])))
(defn- send-data-subject [{:keys [consus-user consus-config] :as component} obj]
  (let [path (format "%s/processors/%s/subjects/%s/_info" consus-user (:processorId obj) (:id obj))]
    (upspin/send consus-config path obj)))

(s/fdef send-pii-data
        :args (s/cat
               :component (s/keys :req-un [::consus-user ::consus-config])
               :obj (s/keys :req-un [::id ::type ::piiType ::subjectId ::processorId ::value])))
(defn- send-pii-data [{:keys [consus-user consus-config] :as component} obj]
  (let [path (format "%s/processors/%s/subjects/%s/pii-data/%s/%s" consus-user (:processorId obj) (:subjectId obj) (:id obj) (:piiType obj))]
    (upspin/send consus-config path obj)))

(s/def ::location (s/and string? #(-> % string/blank? not) #(.contains ^String % "/") #(.contains ^String % "@")))
(s/def ::user (s/and string? #(-> % string/blank? not)))
(s/def ::access-request
  (s/keys :req-un [::location ::user]))

;;consus-config not getting through.
(defn- make-api-routes [{:keys [enki-server signer consus-config] :as component}]
  (log/info "***" component enki-server signer consus-config)
  (routes
   (GET "/healthcheck" {:keys []}
     (resp/response "ok"))
   (GET "/data" {:keys [query-params] :as req}
     (cond
       (contains? query-params "file")
       (upspin/get consus-config (get query-params "file"))

       (contains? query-params "dir")
       (upspin/list consus-config (get query-params "dir"))

       :else
       (-> (resp/response (error-result "No file or dir field found")) (resp/status 400))))
   (POST "/data" {:keys [json-params]}
    (try+
     (log/info ::data json-params)
     (if-let [type (keyword (get json-params "type"))]
       (case type
         :metadata-assertion (send-assertion component json-params)
         :share-assertion (send-assertion component json-params)
         :pii-type (send-assertion component json-params)
         :sharing-purpose (send-assertion component json-params)
         :data-subject (send-data-subject component (keywordize-keys json-params))
         :pii-data (send-pii-data component (keywordize-keys json-params))
         (-> (resp/response (error-result (format "Unrecognised type '%s'" type))) (resp/status 400)))
       (-> (resp/response (error-result "No type field found")) (resp/status 400)))
     (catch [] e
          (log/info ::data ::error e "Upspin Access error")
          (-> (resp/response (error-result (str e))) (resp/status 400)))))
   (POST "/access" {:keys [params] :as req}
     (if (not (s/valid? ::access-request params))
       (-> (resp/response (error-result (s/explain-str ::access-request params))) (resp/status 400))
       (try+
        (resp/response {:type "access" :users (upspin/add-reader consus-config (:location params) (:user params))})
        (catch [] e
          (log/info ::data ::error e "Upspin Access error")
          (-> (resp/response (error-result (str e))) (resp/status 400))))))
   (not-found "Not Found")))

(defn- make-request-handler [component]
  (-> component
      (make-api-routes)
      (wrap-routes wrap-keyword-params)
      (wrap-routes wrap-json-params
                   {:malformed-response
                    {:status 400
                     :headers {"Content-Type" "application/json"}
                     :body "{\"type\":\"error\", \"error\": \"Malformed JSON in request\"}"}})
      (wrap-routes wrap-json-response)
      (wrap-routes wrap-defaults api-defaults)))

(defrecord Router []
  component/Lifecycle

  (start [component]
    component)
  (stop [component]
    component)

  http-server/HttpHandler
  (get-handler [component]
    (make-request-handler component)))

(s/def ::component
  (s/keys :req-un [::consus-config ::enki-server ::signing-key]))