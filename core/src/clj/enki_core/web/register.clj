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
(ns enki-core.web.register
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.web.core :as web]
            [ring.util.response :as resp])
  (:import (java.sql SQLException Connection)))

(defn register
  [components db session {:strs [username password password-confirm]}]
  (with-transaction db [tx]
    (cond
      (str/blank? username)
      (web/render-file components
                       "register"
                       {:error "No username given"
                        :username-error "No username given"}
                       400)

      (and (str/blank? password)
           (str/blank? password-confirm))
      (web/render-file components
                       "register"
                       {:error "No password given"
                        :username username
                        :password-error "No password given"}
                       400)

      (not= password password-confirm)
      (web/render-file components
                       "register"
                       {:error "Passwords don't match"
                        :username username
                        :password-error "Passwords don't match"}
                       400)

      :else
      (try
        (let [user (store/insert-user! db tx username password)]
          (-> (resp/redirect "/" :see-other)
              (assoc :session (assoc session :identity (:name user)))))
        (catch SQLException e
          (log/error e)
          (.rollback ^Connection (:connection tx))
          ;; https://www.postgresql.org/docs/9.6/static/errcodes-appendix.html
          ;; 23505 -> unique_violation
          (if (= "23505"
                 (.getSQLState e))
            (web/render-file components
                             "register"
                             {:error "User with same name already exists"
                              :username username
                              :username-error "User with same name already exists"}
                             400)
            (web/render-file components
                             "register"
                             {:error (str e)}
                             400)))
        (catch Exception e
          (log/error e)
          (.rollback ^Connection (:connection tx))
          (web/render-file components
                           "register"
                           {:error (str e)
                            :username username}
                           400))))))
