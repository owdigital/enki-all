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
  (:require
   [enki-agent.systems :as systems]

   [clojure.java.io :as io]
   [clojure.test :as test]
   [reloaded.repl :as repl]
   [clojure.repl]
   [environ.core :refer [env]]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [cemerick.pomegranate :refer [add-dependencies]])
  (:import ch.qos.logback.classic.joran.JoranConfigurator
           ch.qos.logback.classic.LoggerContext
           org.slf4j.LoggerFactory))

(defn run-tests []
  (test/run-all-tests #"^enki.*test$"))

(repl/set-init!
 #(systems/system {::systems/enki-server-url (get env :enki-server-url "http://localhost:3000/")
                   ::systems/enki-key-file "sign.key"
                   ::systems/port 3010
                   ::systems/consus-user (get env :consus-user "test-agent@test.labshift.io")
                   ::systems/bank-name (get env :bank-name "sample-bank")
                   ::systems/oauth-client-id (get env :oauth-client-id "sample-bank-client")}))

(defn add-dep [coord]
  (add-dependencies :coordinates [coord]
                    :repositories (merge cemerick.pomegranate.aether/maven-central
                                         {"clojars" "https://clojars.org/repo"})))

(defn reload-logback []
  (let [context ^LoggerContext (LoggerFactory/getILoggerFactory)
        configurator (JoranConfigurator.)
        config (io/resource "logback.xml")]
    (assert config)
    (.reset context)
    (.setContext configurator context)
    (.doConfigure configurator config)))

(defn pst []
  (clojure.repl/pst))