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
(ns enki-agent.systems
  (:require
   [enki-agent.enki-service :as enki]
   [enki-agent.http-server :as server]
   [enki-agent.command.sign :as sign]
   [enki-agent.routing :as routing]

   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [clojure.spec.alpha :as s]))

(s/def ::enki-server-url string?)
(s/def ::enki-key-file string?)
(s/def ::consus-user string?)
(s/def ::consus-config (s/nilable string?))
(s/def ::bank-name string?)
(s/def ::agent-url string?)
(s/def ::oauth-client-id string?)

(s/def ::config
  (s/keys :req [::enki-server-url ::enki-key-file ::consus-user ::consus-config ::bank-name ::oauth-client-id]))

(s/def ::enki-server :enki-agent.enki-service/component)
(s/def ::signer :enki-agent.command.sign/component)
(s/def ::routing :enki-agent.routing/component)
(s/def ::http :enki-agent.http-server/component)
(s/def ::system
  (s/keys :req [::enki-server ::signer ::http ::routing]))

(def config-spec ::config)
(s/fdef system :args (s/cat :config config-spec) :ret ::system)
(defn system [config]
  (s/assert config-spec config)
  (log/info "config->" config)
  (let [{:keys [::enki-server-url ::enki-key-file ::port ::consus-user ::consus-config ::bank-name ::agent-url ::oauth-client-id]} config
        enki-server (enki/make enki-server-url bank-name consus-user agent-url oauth-client-id)
        signer (sign/map->AssertionSigner {:signing-key-file enki-key-file})
        http (server/instance port)
        router (routing/map->Router {:consus-user consus-user :consus-config consus-config})]

    (component/system-map
     ::enki-server enki-server
     ::signer signer
     ::routing (component/using router {:enki-server ::enki-server :signer ::signer})
     ::http (component/using http {:app ::routing}))))

(s/fdef run-until-done :args ::system)
(defn run-until-done [me]
  (server/run-until-done (::http me)))
