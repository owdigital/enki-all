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
(ns enki-core.hydra.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::client_name string?)
(s/def ::client_secret string?)
(s/def ::client_uri string?)
(s/def ::description string?)
(s/def ::grant_type string?)
(s/def ::token_type string?)
(s/def ::expires_in integer?)
(s/def ::id_token string?)
(s/def ::access_token string?)
(s/def ::grant_types (s/+ ::grant_type))
(s/def ::scope string?)
(s/def ::response_types (s/* string?))
(s/def ::actions (s/* string?))
(s/def ::conditions (s/map-of string? string?))
(s/def ::resources (s/* string?))
(s/def ::subjects (s/* string?))
(s/def ::redirect_uris (s/* string?))
(s/def ::effect string?)

(s/def ::token-request (s/keys :req [::grant-type ::scope]))
(s/def ::create-client-request (s/keys :req [::grant_types ::id ::client_name ::response_types ::scope]
                                       :opt [::redirect_uris]))
(s/def ::create-policy-request (s/keys :req [::actions ::effect ::resources ::subjects ::id]))

; Is this delete necessary?
(s/def ::delete-request (s/keys :opt-un []))

(s/def ::token-response (s/keys :req [::access_token ::expires_in ::token_type ::scope]
                                :opt [::id_token]))

(s/def ::create-client-response (s/keys :req [::id ::client_name ::client_secret ::redirect_uris ::grant_types ::response_types ::scope]
                                        :opt [::owner ::policy_uri ::tos_uri ::client_uri ::logo_uri ::contacts ::public]))
(s/def ::list-client-response (s/keys :req [::id ::client_name ::redirect_uris ::grant_types ::response_types ::scope]
                                      :opt [::owner ::policy_uri ::tos_uri ::client_uri ::logo_uri ::contacts ::public]))
(s/def ::list-policies-response (s/keys :req [::id ::actions ::conditions ::description ::effect ::resources ::subjects]
                                        :opt [::owner ::policy_uri ::tos_uri ::client_uri ::logo_uri ::contacts ::public]))
(s/def ::create-policy-response (s/keys :req [::id ::description ::subjects ::effect ::resources ::actions]
                                        :opt [::conditions]))
(s/def ::delete-response (s/keys :opt-un []))

(s/def ::client-info-response ::list-client-response)
(s/def ::list-clients-response (s/map-of string? ::client-info-response))

(s/def ::use string?)
(s/def ::kty string?)
(s/def ::n string?)
(s/def ::e string?)
(s/def ::d string?)
(s/def ::p string?)
(s/def ::q string?)

(defmulti key-type ::kid)

(defmethod key-type "public" [_]
  (s/keys :req [::use ::kty ::n ::e]))

(defmethod key-type "private" [_]
  (s/keys :req [::use ::kty ::n ::e ::d ::p ::q]))

(s/def ::key (s/multi-spec key-type ::kid))

(s/def ::keys (s/coll-of ::key :count 1))

(s/def ::get-keys-response (s/keys :req [::keys]))
