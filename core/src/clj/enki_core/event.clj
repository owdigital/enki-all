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
(ns enki-core.event
  (:require [clojure.spec.alpha :as s]))

(defmulti command-type first)
(defmulti process :command)

(s/def ::event (s/multi-spec command-type (fn [[_ value] command] [command value])))

(def events (ref []))

(defn record-event! [e]
  (alter events conj e))

(defn replay-events! []
  (doseq [e @events] (process e)))

(defn ingest!
  [event]
  (if (s/valid? ::event event)
    (dosync
     (record-event! event)
     (process (s/conform ::event event)))
    (s/explain ::event event)))
