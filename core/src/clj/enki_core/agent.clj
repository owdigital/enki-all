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
(ns enki-core.agent
  (:require
   [clj-http.client :as client]
   [clojure.tools.logging :as log]
   [enki-core.time :as time]
   [enki-core.util :refer [join-url]]
   [slingshot.slingshot :refer [try+ throw+]])
  (:import
   (java.util UUID)))

(defn client-post [path data]
  (try+
   (client/post path
                {:form-params data
                 :content-type :json
                 :as :json})
   (catch [:status 400] {:keys [body]}
     (log/warnf "Bad request: %s" data)
     (log/warnf "Response: %s" body)
     (throw+))))

; FIXME: Both /access and /data are being allowed without authorisation, which needs fixing

(defn allow-metadata-access [metadata-info consus-user-name banks]
  (doseq [info metadata-info
          :let [bank (get banks (:bank_id info))]]
    (log/infof "Unlocking %s for %s via %s" (:location info) consus-user-name (:agent bank))
    (client-post (join-url (:agent bank) "access")
                 {:location (:location info)
                  :user consus-user-name})))

(defn make-share-assertion [bank metadata-id purpose-id]
  (let [now       (time/utc-now)
        assertion {:type "share-assertion"
                   :id (str (UUID/randomUUID))
                   :metadataId metadata-id
                   :sharingProcessorId (:id bank)
                   :purposeId purpose-id
                   :createdAt now
                   :consentStart now
                   :consentEnd (.plusYears now 1)}]
    (client-post (join-url (:agent_url bank) "data")
                 assertion)))
