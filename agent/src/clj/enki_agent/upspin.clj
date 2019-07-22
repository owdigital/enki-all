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
(ns enki-agent.upspin
  (:require
   [clojure.string :as string]
   [clojure.java.shell :refer [sh]]
   [clojure.tools.logging :as log]
   [clojure.data.json :as json]
   [ring.util.response :as resp]
   [clojure.set :as set]
   [slingshot.slingshot :refer [try+ throw+]])
  (:refer-clojure :exclude [send get list]))

(defn- error-result [msg]
  {:type "error" :error msg})

(defn- config-str [config]
  (if config
    (format "-config=%s/config" config)
    ""))

(defn- make-upspin-path [config path]
  (loop [items (drop-last (string/split path #"/")) ; We assume the last item is a file
         sofar ""]
    (if-let [current (first items)]
      (let [newpath (if (string/blank? sofar) current (str sofar "/" current))
            configstr (config-str config)
            ucmd (clojure.core/list "upspin" configstr "mkdir" newpath)
            _ (log/info ::make-upspin-path ucmd)
            {:keys [exit out err]} (apply sh ucmd)]
        (cond
          (zero? exit)
          (do
            (log/infof "Made %s" newpath)
            (recur (rest items) newpath))

          (string/includes? err "client.MakeDirectory: item already exists")
          (recur (rest items) newpath)

          :else
          {:result false :reason err}))
      {:result true :reason "No more items"})))

(defn send [config path obj]
  (let [mkpath (make-upspin-path config path)]
    (if (:result mkpath)
      (let [ucmd (clojure.core/list "upspin" (config-str config) "put" "-glob=false" path :in (json/write-str obj))
            _ (log/info ::send ucmd)
            {:keys [exit out err]} (apply sh ucmd)]
        (if (zero? exit)
          (-> (resp/response {:type "consus" :location path}) (resp/status 201))
          (-> (resp/response (error-result err)) (resp/status 500))))
      (-> (resp/response (:reason mkpath)) (resp/status 500)))))

(defn get [config path]
  (let [ucmd (clojure.core/list "upspin" (config-str config) "get" "-glob=false" path)
        _ (log/info ::get ucmd)
        {:keys [exit out err]} (apply sh ucmd)]
    (cond
      (zero? exit)
      (try+
       (-> (resp/response {:type "consus" :data (json/read-str out)}) (resp/status 200))
       (catch #(string/includes? (.getMessage ^Exception %) "JSON error") {}
         (-> (resp/response (error-result (format "Non-JSON data in Consus: %s" out))) (resp/status 500))))

      (string/includes? err "item does not exist")
      (-> (resp/response (error-result "Not found")) (resp/status 404))

      (string/includes? err "information withheld")
      (-> (resp/response (error-result "Forbidden")) (resp/status 403))

      :else
      (-> (resp/response (error-result err)) (resp/status 500)))))

(defn list [config path]
  (let [ucmd (clojure.core/list "upspin" (config-str config) "ls" path)
        _ (log/info ::list ucmd)
        {:keys [exit out err]} (apply sh ucmd)]
    (cond
      (zero? exit)
      (-> (resp/response {:type "consus" :items (map #(hash-map :short (last (string/split % #"/")) :long %) (string/split out #"\n"))}) (resp/status 200))

      (string/includes? err "item does not exist")
      (-> (resp/response (error-result "Not found")) (resp/status 404))

      :else
      (-> (resp/response (error-result err)) (resp/status 500)))))

(defn- directory [path]
  (subs path 0 (string/last-index-of path "/")))

(defn- access-path [path]
  (str (directory path) "/" "Access"))

(defn- get-owner [path]
  (first (string/split path #"/")))

(defn- parse-access-line [line]
  (let [[kind users] (string/split line #":")
        kind (string/trim kind)
        users (mapv string/trim (string/split users #","))]
    {kind users}))

(defn- list-access [config path]
  (let [owner (get-owner path)
        ucmd (clojure.core/list "upspin" (config-str config) "get" (access-path path))
        _ (log/info ::list-access ucmd)
        {:keys [exit out err]} (apply sh ucmd)]
    (cond
      (zero? exit)
      (let [lines (string/split out #"\n")
            perms (apply merge (map parse-access-line lines))]
        (apply set/union (vals perms)))

      (string/includes? err "item does not exist")
      [owner]

      :else
      (do
        (log/errorf "Access Path: %s" (access-path path))
        (throw+ {:type :upspin-error :error err})))))

(defn- user-exists [config user]
  (let [ucmd (clojure.core/list "upspin" (config-str config) "user" user)
        _ (log/info ::user-exists ucmd)
        {:keys [exit out err]} (apply sh ucmd)]
    (zero? exit)))

(defn add-reader [config path user]
  (log/infof "Adding reader to %s for %s" path user)
  (if-not (user-exists config user)
    (throw+ {:type :user-not-exists :error (format "User %s not on keyserver" user)})
    (let [readers (-> (list-access config path) (conj user) set (disj (get-owner path)))
          data (string/join "\n" [(format "Write: %s" (get-owner path))
                                  (if (empty? readers) "" (format "Read: %s" (string/join "," readers)))])
          ucmd (clojure.core/list "upspin" (config-str config) "put" "-glob=false" (access-path path) :in data)
          _ (log/info ::add-reader ucmd)
          {:keys [exit out err]} (apply sh ucmd)]
      (if-not (zero? exit)
        (do
          (log/errorf "Error while trying to write out (to %s) readers: %s. Path: %s" (list-access config path) readers (access-path path))
          (log/errorf "Data was '%s'" data)
          (throw+ {:type :upspin-error :error err}))
        (let [ucmd (clojure.core/list "upspin" (config-str config) "share" "-fix" path)
              _ (log/info ::add-reader ucmd)
              {:keys [exit out err]} (apply sh ucmd)]
          (if (zero? exit)
            (list-access config path)
            (do
              (log/errorf "Share Path: %s" (list-access config path))
              (throw+ {:type :upspin-error :error err}))))))))