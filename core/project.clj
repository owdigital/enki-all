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
(defproject enki-core "0.1.1-SNAPSHOT"
  :description "Enki Core"
  :url "https://github.com/lshift/enki-core"
  :min-lein-version "2.7.1" ; To avoid https://github.com/technomancy/leiningen/issues/2066

  :source-paths ["src/clj"]
  :pedantic? :abort

  :dependencies [[org.clojure/clojure "1.10.0"]

                 [com.stuartsierra/component "0.4.0"]
                 [reloaded.repl "0.2.4"]
                 [environ "1.1.0"]

                 ;; logging
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.logging "0.4.1"]
                 [ring-logger "1.0.1"]

                 ;; Web
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.8.1"]
                 [aleph "0.4.6"]
                 [compojure "1.6.1"]
                 [metosin/compojure-api "2.0.0-alpha18"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/spec-tools "0.5.1"]

                 [metosin/ring-http-response "0.9.1"]
                 [clj-http "3.10.0"]
                 [hbs "1.0.3"]

                 ;; Security
                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0" :exclusions [buddy/buddy-core]]
                 [buddy/buddy-sign "3.0.0"]
                 [org.bouncycastle/bcpkix-jdk15on "1.61"]
                 [com.nimbusds/nimbus-jose-jwt "7.2.1" :exclusions [net.minidev/json-smart]]
                 [net.minidev/json-smart "2.3"]
                 [enki/common "0.20.1"]
                 [commons-lang/commons-lang "2.6"]

                 ;; database
                 [org.clojure/java.jdbc "0.7.9"]
                 [hikari-cp "2.7.1"]
                 [org.flywaydb/flyway-core "5.2.4"]
                 [org.postgresql/postgresql "42.2.5"]
                 [com.layerware/hugsql "0.4.9"]]

  :managed-dependencies [[clj-time "0.15.1"]
                         [com.google.code.findbugs/jsr305 "3.0.2"]
                         [commons-io "2.6"]
                         [joda-time "2.10.2"]
                         [potemkin "0.4.5"]
                         [com.fasterxml.jackson.core/jackson-core "2.9.9"]]

  :main enki-core.main

  :plugins [[lein-environ "1.1.0" :hooks false]
            [lein-shell "0.5.0"]]
  :repositories {"local" {:url "file:lib" :username "" :password ""}}
  :aliases {"tests" ["run" "-m" "circleci.test/dir" :project/test-paths]
            "build-with-webpack" ["do"
                                  ["shell" "rm" "-rf" "resources/public/build"]
                                  ["shell" "yarn" "run" "webpack" "-p"]
                                  ["uberjar"]]}

  :global-vars {*warn-on-reflection* true}
  :profiles
  {:dev {:source-paths ["dev" "src/clj"]
         :env {:http-port "3000"
               :base-uri-bank-a #=(eval (str "http://" (System/getenv "HOSTNAME") ":9001"))
               :base-uri-bank-b #=(eval (str "http://" (System/getenv "HOSTNAME") ":9002"))
               :database-url "jdbc:postgresql://localhost:5432/enki-core?user=postgres"
               :hydra-server-url #=(eval (str "http://" (System/getenv "HOSTNAME") ":5444"))
               :hydra-admin-login "admin"
               :hydra-admin-password "demo-password"
               :hydra-client-id "consent-app"
               :hydra-client-secret "consent-secret"
               :oidc-target-url-bank-a "http://localhost:9001/api/oidc"
               :oauth-base-uri-bank-a #=(eval (str "http://" (System/getenv "HOSTNAME") ":4444"))
               :oauth-client-id-bank-a "enki-consumer"
               :oauth-client-secret-bank-a "enki-secret"
               :oidc-target-url-bank-b "http://localhost:9002/api/oidc"
               :oauth-base-uri-bank-b #=(eval (str "http://" (System/getenv "HOSTNAME") ":4445"))
               :oauth-client-id-bank-b "enki-consumer"
               :oauth-client-secret-bank-b "enki-secret"}
         :flyway {:driver "org.postgresql.Driver"
                  :url #=(eval (System/getenv "DATABASE_URL"))}
         :plugins [[lein-auto "0.1.3"]
                   [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure]]
                   [com.github.metaphor/lein-flyway "4.0.3" :exclusions [commons-io]]]
         :aliases {"fmt"
                   ["auto" "do" ["cljfmt" "fix"] ["cljfmt" "fix" "project.clj"]]}
         :repl-options {:init-ns user}
         :dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]
                        [ring/ring-jetty-adapter "1.7.1"]
                        [org.clojure/test.check "0.9.0"]
                        [circleci/circleci.test "0.4.2"]
                        [enlive "1.1.6"]]}
   :uberjar {:aot :all
             :omit-source true
             :uberjar-name "enki.jar"}})
