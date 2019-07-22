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
(ns enki-core.handler
  (:require [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [defroutes routes wrap-routes]]
            [compojure.route :as route]
            [cheshire.generate :refer [add-encoder]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [enki-core.api :refer [api-routes]]
            [enki-core.time :as time]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.web.core :refer [unauthorized-handler]]
            [enki-core.web.oidc :refer [oidc-routes]]
            [enki-core.web.public :refer [app-routes]]
            [enki-core.web.secure :refer [secure-routes]]
            [enki-core.web.secure-api :refer [secure-api-routes]]
            [muuntaja.core :as m]
            [muuntaja.middleware :as mw]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session.memory :as mem])
  (:import (java.time LocalDateTime)
           (com.fasterxml.jackson.core JsonGenerator)))

(add-encoder LocalDateTime
             (fn [c ^JsonGenerator jsonGenerator]
               (.writeString jsonGenerator (time/to-rfc3339-string (time/localdatettime-to-zoned c)))))

(defn wrap-exception
  [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           {:status 500
            :message (str e)
            :body "Something went wrong"}))))

(defroutes no-page
  (route/not-found "Not Found!"))

(defn- authfn
  [db x]
  (with-transaction db [tx]
    (store/user-exists? db tx x)))

(defn- auth-backend
  [components]
  (session-backend {:unauthorized-handler (unauthorized-handler components)
                    :authfn               (fn [x] (authfn (:db components) x))}))

(def muuntaja
  (m/create
   (update-in m/default-options [:formats] dissoc "application/edn" "application/transit+json" "application/transit+msgpack")))

(defn wrap-auth
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (throw-unauthorized))))

;; Provide your own atom to `memory-store` to debug sessions.
;(def client-sessions (atom {}))

;; See: https://stackoverflow.com/a/28017586/1224343
;; for details
(defn make-app
  [components]
  (let [auth           (auth-backend components)
        store          (mem/memory-store)
        new-defaults   (-> site-defaults
                           (assoc-in [:session :store] store)
                           (assoc :proxy true))]
    (routes
     (-> (app-routes components)
         (wrap-routes wrap-defaults new-defaults))
     (-> (secure-routes components)
         (wrap-routes wrap-auth)
         (wrap-routes wrap-authorization auth)
         (wrap-routes wrap-authentication auth)
         (wrap-routes wrap-defaults new-defaults))
     (-> (secure-api-routes components)
         (wrap-routes wrap-exception)
         (wrap-routes wrap-auth)
         (wrap-routes wrap-authorization auth)
         (wrap-routes wrap-authentication auth)
         (wrap-routes mw/wrap-format muuntaja)
         (wrap-routes wrap-defaults (update new-defaults [:responses] dissoc :default-charset :content-types)))
     (-> (api-routes components)
         (wrap-routes wrap-defaults (update api-defaults [:responses] dissoc :default-charset :content-types)))
     (-> (oidc-routes components)
         (wrap-routes wrap-auth)
         (wrap-routes wrap-authorization auth)
         (wrap-routes wrap-authentication auth)
         (wrap-routes wrap-defaults
                      (-> new-defaults (assoc-in [:session :cookie-attrs :same-site] :lax))))
     no-page)))

(defrecord Handler [db hydra-token registry hb-context app]
  component/Lifecycle

  (start [component]
    (if app
      component
      (assoc component :app (make-app {:db db
                                       :context hb-context
                                       :registry registry
                                       :hydra-token hydra-token}))))

  (stop [component]
    (assoc component :app nil)))

(defn instance
  []
  (map->Handler {}))
