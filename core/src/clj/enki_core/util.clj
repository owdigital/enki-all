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
(ns enki-core.util
  (:require [environ.core :refer [env]])
  (:refer-clojure :exclude [path])
  (:import (java.net URL)
           (java.util Base64)))

(defn join-url
  [^String base-url ^String path]
  (str (URL. (URL. base-url) path)))

(defn get-banks-from-env
  []
  [{:name                "bank-a"
    :url                 (env :oidc-target-url-bank-a)
    :oauth-base-uri      (env :oauth-base-uri-bank-a)
    :oauth-authorize-uri (env :oauth-authorize-uri-bank-a)
    :oauth-client-id     (env :oauth-client-id-bank-a)
    :oauth-client-secret (env :oauth-client-secret-bank-a)}
   {:name                "bank-b"
    :url                 (env :oidc-target-url-bank-b)
    :oauth-base-uri      (env :oauth-base-uri-bank-b)
    :oauth-authorize-uri (env :oauth-authorize-uri-bank-b)
    :oauth-client-id     (env :oauth-client-id-bank-b)
    :oauth-client-secret (env :oauth-client-secret-bank-b)}
   {:name                "iron-bank"
    :url                 (env :oidc-target-url-iron-bank)
    :oauth-base-uri      (env :oauth-base-uri-bank-iron-bank)
    :oauth-authorize-uri (env :oauth-authorize-uri-bank-iron-bank)
    :oauth-client-id     (env :oauth-client-id-iron-bank)
    :oauth-client-secret (env :oauth-client-secret-iron-bank)}])

(defn get-bank-urls-from-env
  []
  [{:name "bank-a"
    :url (env :base-uri-bank-a)}
   {:name "bank-b"
    :url (env :base-uri-bank-b)}])

(defn base64encode
  "Base64 encodes a byte[] to string"
  [data]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder data)))
