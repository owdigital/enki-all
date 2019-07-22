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
(defproject enki/common "0.20.1"
  :description "ENKI common clojure component"
  :license {:name "Private"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.bouncycastle/bcprov-jdk15on "1.61"]
                 [commons-lang/commons-lang "2.6"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [commons-codec/commons-codec "1.12"]
                 [commons-io/commons-io "2.6"]]
  :main nil
  :java-source-paths ["src/java"]
  :global-vars {*warn-on-reflection* true}
  :test-paths ["test"]
  :pedantic? :abort
  :auto {:default {:file-pattern #"\.(clj|cljs|cljx|cljc|java)$"}}

  :profiles
  {:dev
   {:source-paths ["dev"]
    :repl-options {:init-ns user}
    :dependencies [[org.apache.httpcomponents/httpcore "4.4.11"]
                   [org.clojure/tools.namespace "0.2.11"]
                   [com.cemerick/pomegranate "1.1.0" :exclusions [org.slf4j/jcl-over-slf4j org.slf4j/slf4j-api commons-io]]]
    :plugins
    [[lein-auto "0.1.2"]
     [lein-cljfmt "0.5.7" :exclusions [org.clojure/clojure]]]
    :aliases {"fmt" ["auto" "fmt-nonauto"]
              "fmt-nonauto" ["do" ["cljfmt" "fix"] ["cljfmt" "fix" "project.clj"]]}}
   :test {:plugins [[lein-test-report-junit-xml "0.2.0"]]}})
