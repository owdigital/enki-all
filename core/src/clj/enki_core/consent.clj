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
(ns enki-core.consent
  (:require [enki-core.event :as e]
            [clojure.spec.alpha :as s]))

;;; Store

(def store (ref {}))

(defn last-cqs-id [subject]
  (-> (@store subject) last :hash))

(defn in-order? [subject hash]
  (= (last-cqs-id subject) hash))

(defn alter-store [subject previous f & args]
  (if (in-order? subject (second previous))
    (apply alter store update subject f args)
    :reject))

(defn append-store [subject previous data]
  (alter-store subject previous concat [(assoc data :hash (hash data))]))

(defn remove-store [subject previous f]
  (alter-store subject previous (partial remove f)))

(s/def ::subject int?)
(s/def ::object string?)
(s/def ::purposes (s/coll-of string?))
(s/def ::period keyword?)
(s/def ::places (s/coll-of keyword?))
(s/def ::hash int?)
(s/def ::previous (s/or :hash int? :root nil?))

(s/def ::give-consent-data (s/keys :req-un [::subject ::object ::purposes ::period ::places ::previous]))
(s/def ::revoke-consent-data (s/keys :req-un [::subject ::previous ::hash]))

(defmethod e/command-type :give-consent [_]
  (s/cat :command keyword? :data ::give-consent-data))

(defmethod e/command-type :revoke-consent [_]
  (s/cat :command keyword? :data ::revoke-consent-data))

(defmethod e/process :give-consent [{{:keys [subject previous] :as data} :data}]
  (let [data (dissoc data :previous)]
    (append-store subject previous data)))

(defmethod e/process :revoke-consent [{{:keys [subject previous hash]} :data}]
  (remove-store subject previous #(= (% :hash) hash)))

;;; Helpers

(defn generate-give-consent [subject data]
  (let [subject-id (hash subject)]
    [:give-consent (merge data {:subject subject-id
                                :previous (last-cqs-id subject-id)})]))

(defn generate-revoke-consent [subject id]
  (let [subject-id (hash subject)]
    [:revoke-consent {:subject subject-id
                      :previous (last-cqs-id subject-id)
                      :hash id}]))
