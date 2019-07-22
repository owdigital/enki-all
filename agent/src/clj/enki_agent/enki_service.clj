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
(ns enki-agent.enki-service
  (:require [clj-http.client :as http]
            [com.stuartsierra.component :as component]
            [clojure.spec.alpha :as s]
            [enki-agent.endpoints :refer [endpoints]])
  (:import (java.net URI)
           (java.util Base64)))

(s/def ::enki-server-url string?)

(s/def ::config
  (s/keys :req [::enki-server-url]))

(defprotocol EnkiService
  (send-signed-assertion [this type data endpoint])
  (register-key [enki-server data]))

(defn- base64encode
  [data]
  (let [encoder (Base64/getEncoder)]
    (.encodeToString encoder data)))

(s/def ::component
  (s/keys :req-un [::enki-server-url]))

(defrecord HttpEnkiServer [^URI enki-server-url ^String bank-name ^String consus-user ^String agent-url ^String oauth-client-id]
  component/Lifecycle
  (start [{:keys [enki-server-url bank-name] :as this}]
    (assert enki-server-url (str "enki-server-url variable not in " (pr-str this)))
    this)
  (stop [this] this)

  EnkiService
  (send-signed-assertion
    [this type data endpoint]
    (let [^URI url (.resolve enki-server-url ^String endpoint)
          data-name (case type
                      :pii-type "pii_type"
                      :sharing-purpose "sharing_purpose"
                      "assertion")]
      (http/post (.toASCIIString url) {:form-params {"bank_name" bank-name
                                                     data-name (base64encode data)}})))
  (register-key
    [{:keys [enki-server-url]} data]
    (let [^URI url (.resolve ^URI enki-server-url ^String (:register endpoints))]
      (http/post (.toASCIIString url) {:form-params {"bank_name" bank-name
                                                     "signing_key" (base64encode data)
                                                     "consus_user" consus-user
                                                     "agent_url" agent-url
                                                     "oauth_client_id" oauth-client-id}}))))

(defn make [enki-server-url bank-name consus-user agent-url oauth-client-id]
  (let [enki-server-url (URI/create enki-server-url)]
    (->HttpEnkiServer enki-server-url bank-name consus-user agent-url oauth-client-id)))
