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
(ns enki-core.handlebars
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [hbs.core :as hbs]
            [hbs.helper :refer [defhelper register-helper!]]
            [ring.util.codec :refer [form-encode]])
  (:import (com.github.jknack.handlebars Handlebars Handlebars$SafeString Options)))

(defhelper to-json [ctx ^Options options]
  (json/generate-string ctx))

(defhelper lookup-dict [ctx ^Options options]
  (get ctx (.param options 0)))

(defhelper make-query-params [fragment ^Options options]
  (let [raw-params   (.-params options)
        params       (for [i (range (/ (alength raw-params) 2))
                           :let [j (* 2 i)
                                 k (inc j)]
                           :when (not (str/blank? (aget raw-params k)))]
                       [(aget raw-params j)
                        (aget raw-params k)])]
    (if (seq params)
      (Handlebars$SafeString. (str fragment "?" (form-encode (into {} params))))
      (Handlebars$SafeString. fragment))))

(defn get-bundles-files
  [dev?]
  (let [manifest    (io/resource "public/build/build-manifest.json")
        dev-assets  {"bundle.css" "bundle.css"
                     "vendor.js" "vendor.js"
                     "bundle.js" "bundle.js"}]
    (if dev?
      dev-assets
      (if manifest
        (json/parse-string (slurp manifest))
        (do
          (log/warn "Unable to find build-manifest.json. So using dev mode assets")
          dev-assets)))))

(defn setup-handlebars!
  [dev?]
  (let [registry   (hbs/registry
                    (hbs/composite-loader (hbs/classpath-loader "/templates" ".hbs")
                                          (hbs/classpath-loader "/templates/partials" ".hbs"))
                    :auto-reload? dev?)]
    (register-helper! registry "json" to-json)
    (register-helper! registry "lookupdict" lookup-dict)
    (register-helper! registry "make-query-params" make-query-params)
    registry))
