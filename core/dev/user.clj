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
(ns user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :as test]
            [enki-core.system :refer [instance] :as system]
            [environ.core :refer [env]]
            [reloaded.repl :as repl]
            [clojure.repl :refer [pst]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

(repl/set-init! #(instance {::system/dev? true
                            ::system/http-port (env :http-port)
                            ::system/database-url (env :database-url)}))

(defn run-tests []
  (test/run-all-tests #"^enki-core.*test$"))