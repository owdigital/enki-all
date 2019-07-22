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
(ns enki-core.system
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [enki-core.handler :as handler]
            [enki-core.handlebars :as handlebars]
            [enki-core.hydra.consent :as hydra]
            [enki-core.server :as server]
            [enki-core.store.component :as store]
            [environ.core :refer [env]]))

(defn port?
  [x]
  (if (integer? x)
    x
    (if (string? x)
      (try
        (Integer/parseInt x)
        (catch Exception _
          ::s/invalid))
      ::s/invalid)))

(s/def ::dev? boolean?)
(s/def ::http-port (s/conformer port?))
(s/def ::database-url string?)
(s/def ::config (s/keys :req [::http-port ::database-url ::dev?]))

(defn instance
  [config]
  (let [cconfig  (s/conform ::config config)]
    (if (= cconfig ::s/invalid)
      (do
        (log/warn (s/explain-str ::config config))
        (s/assert ::config config))
      (let [{:keys [::http-port ::dev?]}  cconfig]
        (merge
         (component/system-map
          :registry    (handlebars/setup-handlebars! dev?)
          :hb-context  {:webpackBaseUrl "build"
                        :bundles (handlebars/get-bundles-files dev?)}
          :hydra-token (hydra/token-instance
                        (env :hydra-client-id)
                        (env :hydra-client-secret))
          :handler     (component/using
                        (handler/instance)
                        [:registry :hb-context :hydra-token :db])
          :web-server (component/using
                       (server/instance http-port)
                       [:db :handler :migrator]))
         (store/get-database-components cconfig))))))
