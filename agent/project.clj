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
(defproject enki-agent "0.1.0-SNAPSHOT"
  :description "ENKI agent"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [enki/common "0.20.1"]
                 [clj-http "3.10.0"]
                 [cheshire "5.8.1"]
                 [org.clojure/tools.cli "0.4.2"]
                 [environ "1.1.0"]
                 [com.stuartsierra/component "0.4.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.4.0"]
                 [aleph "0.4.6"]
                 [compojure "1.6.1"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [org.clojure/data.json "0.2.6"]

                 ;; logging
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ring-logger "1.0.1"]

                 ;; Security
                 [org.bouncycastle/bcpkix-jdk15on "1.61"]
                 [commons-lang/commons-lang "2.6"]]

  :managed-dependencies [[commons-logging "1.2"]
                         [riddley "0.2.0"]
                         [potemkin "0.4.5"]
                         [org.slf4j/slf4j-api "1.7.26"]
                         [commons-codec "1.12"]
                         [commons-io "2.6"]]

  :global-vars {*warn-on-reflection* true}
  :pedantic? :abort

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clj"]
  :main ^:skip-aot enki-agent.core

  :target-path "target/%s"

  :plugins [[lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure]]
            [lein-environ "1.1.0"]]

  :repositories {"local" {:url "file:lib" :username "" :password ""}}
  :profiles {:dev  {:env {:enki-server-url "http://localhost:3000"}
                    :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                   [com.cemerick/pomegranate "1.1.0" :exclusions [org.slf4j/jcl-over-slf4j]]
                                   [reloaded.repl "0.2.4" :exclusions [org.clojure/tools.namespace]]]
                    :source-paths ["dev"]
                    :repl-options {:init-ns user}
                    :plugins [[lein-auto "0.1.2"]]
                    :aliases {"fmt" ["auto" "do" ["cljfmt" "fix"] ["cljfmt" "fix" "project.clj"]]}}
             :test {:plugins [[lein-test-report-junit-xml "0.2.0"]]}
             :uberjar {:aot :all
                       :omit-source true
                       :uberjar-name "enki-agent.jar"}})
