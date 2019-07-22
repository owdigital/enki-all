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
(ns enki-core.hydra.api
  "API calls to create/update/get/delete clients and policies.
  Client can be either a consumer app or a consent client. Creating a new client
  returns client secret to be used for their authentication.
  Consumer app clients are created with a set of scopes they are allowed to request.
  Policies are used for access control of clients and users."
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [enki-core.hydra.spec :as hspec]
            [enki-core.util :as util]
            [ring.util.codec :refer [url-encode]]
            [slingshot.slingshot :refer [throw+ try+]]
            [clojure.tools.logging :as log]))

(def server-address (env :hydra-server-url))

(defn make-spec-keyword
  [x]
  (keyword "enki-core.hydra.spec" x))

(defn map-dict
  [xs f]
  (into {} (for [[k v] xs] [k (f v)])))

(defn spec-convert-response
  [x]
  (let [json-response (json/parse-string x make-spec-keyword)]
    json-response))

(defn spec-convert-client-list
  [x]
  (let [client-list-json (json/parse-string x)
        spec-converted   (map-dict client-list-json (comp spec-convert-response json/generate-string))]
    spec-converted))

(defn spec-convert-policy-list
  [x]
  (let [policy-list-json (json/parse-string x)
        spec-converted   (map-dict policy-list-json (comp spec-convert-response json/generate-string))]
    spec-converted))

(defn incorrect-response-exception
  [explanation]
  (throw (ex-info "Invalid server response" {:explanation explanation})))

(defn post-request
  [endpoint query-params]
  (let [url (util/join-url server-address endpoint)]
    (try+
     (let [response (http/post url query-params)
           body (:body response)]
       (spec-convert-response body))
     (catch Object _
       (log/errorf "Error while trying to get %s with %s" url (pr-str query-params))
       (throw+)))))

(defn post-request-credentials
  [form-params endpoint use-json? login password]
  (let [query-params    {:form-params form-params
                         :basic-auth [login password]}
        query-params    (if use-json?
                          (assoc query-params :content-type :json)
                          query-params)]
    (post-request endpoint query-params)))

(defn post-request-token
  [form-params endpoint use-json? oauth-token]
  (let [query-params   {:form-params form-params
                        :oauth-token oauth-token}
        query-params   (if use-json?
                         (assoc query-params :content-type :json)
                         query-params)]
    (post-request endpoint query-params)))

(defn get-access-token
  [login password]
  (let [params        {:grant_type "client_credentials"
                       :scope "hydra hydra.clients"}
        endpoint      "/oauth2/token"
        spec-dict     (post-request-credentials params endpoint false login password)]
    (when-not (s/valid? ::hspec/token-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/token-response spec-dict)))
    (get spec-dict ::hspec/access_token)))

(defn code->access-token
  [auth-code username password callback-url]
  (let [params      {:code auth-code
                     :grant_type "authorization_code"
                     :redirect_uri callback-url}
        endpoint    "/oauth2/token"
        spec-dict   (post-request-credentials params endpoint false username password)]
    (when-not (s/valid? ::hspec/token-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/token-response spec-dict)))
    spec-dict))

(defn- create-consent-client-core [client-id oauth-token args]
  (let [endpoint "/clients"
        params   (merge {:grant_types ["client_credentials"
                                       "refresh_token"
                                       "authorization_code"]
                         :id client-id
                         :client_name client-id
                         :response_types ["token" "code" "id_token"]
                         :scope "hydra.keys.get"} args)
        spec-dict (post-request-token params endpoint true oauth-token)]
    (when-not (s/valid? ::hspec/create-client-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-client-response spec-dict)))
    spec-dict))

(defn create-consent-client!
  ([client-id oauth-token]
   (get (create-consent-client-core client-id oauth-token {}) ::hspec/client_secret))
  ([client-id oauth-token client-secret]
   (create-consent-client-core client-id oauth-token {:client_secret client-secret})
   client-secret))

(defn create-consumer-app!
  [client-id callbacks scopes oauth-token]
  (let [scopes        (set/union (set scopes)
                                 #{"openid" "offline" "hydra.clients"})
        endpoint      "/clients"
        params        {:redirect_uris callbacks
                       :grant_types ["authorization_code" "refresh_token"
                                     "client_credentials"]
                       :id client-id
                       :client_name client-id
                       :response_types ["token" "code" "id_token"]
                       :scope (str/join " " scopes)}
        spec-dict     (post-request-token params endpoint true oauth-token)]
    (when-not (s/valid? ::hspec/create-client-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-client-response spec-dict)))
    (get spec-dict ::hspec/client_secret)))

(defn create-app-policy!
  [policy-id client-id oauth-token]
  (let [endpoint     "/policies"
        params       {:actions ["get"]
                      :effect "allow"
                      :resources ["rn:hydra:keys:hydra.consent.<.*>"]
                      :subjects [client-id]
                      :id policy-id}
        spec-dict    (post-request-token params endpoint true oauth-token)]
    (when-not (s/valid? ::hspec/create-policy-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-policy-response spec-dict)))))

(defn create-user-policy!
  [user-id policy-id oauth-token]
  (let [endpoint      "/policies"
        params        {:actions ["get"]
                       :effect "allow"
                       :resources ["rn:hydra:clients:<.*>"]
                       :subjects [user-id]
                       :id policy-id}
        spec-dict     (post-request-token params endpoint true oauth-token)]
    (when-not (s/valid? ::hspec/create-policy-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-policy-response spec-dict)))))

(defn put-request
  [url form-params oauth-token]
  (let [result (http/put url {:form-params form-params
                              :oauth-token oauth-token
                              :content-type :json})]
    (spec-convert-response (:body result))))

(defn update-consumer-app!
  [client-id callbacks scopes oauth-token]
  (let [scopes     (set/union (set scopes)
                              #{"openid" "offline" "hydra.clients"})
        endpoint   (str "/clients/" (url-encode client-id))
        params     {:redirect_uris callbacks
                    :grant_types ["authorization_code" "refresh_token" "client_credentials"]
                    :response_types ["token" "code" "id_token"]
                    :scope (str/join " " scopes)}
        spec-dict  (put-request
                    (util/join-url server-address endpoint)
                    params
                    oauth-token)]
    (when-not (s/valid? ::hspec/create-client-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-client-response spec-dict)))
    spec-dict))

(defn update-policy!
  [policy-id actions effect subjects oauth-token]
  (let [endpoint  (str "/policies/" (url-encode policy-id))
        params    {:actions actions
                   :effect effect
                   :subjects subjects
                   :id policy-id}
        spec-dict (put-request
                   (util/join-url server-address endpoint)
                   params
                   oauth-token)]
    (when-not (s/valid? ::hspec/create-policy-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/create-policy-response spec-dict)))
    spec-dict))

(defn get-request
  [url oauth-token]
  (let [response (http/get
                  url
                  {:oauth-token oauth-token})]
    (spec-convert-response (:body response))))

(defn get-client
  [client-id oauth-token]
  (let [spec-dict (get-request
                   (util/join-url server-address (str "/clients/" (url-encode client-id)))
                   oauth-token)]
    (when-not (s/valid? ::hspec/client-info-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/client-info-response spec-dict)))
    spec-dict))

(defn get-policy
  [policy-id oauth-token]
  (let [spec-dict (get-request
                   (util/join-url server-address (str "/policies/" (url-encode policy-id)))
                   oauth-token)]
    (when-not (s/valid? ::hspec/create-policy-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/client-info-response spec-dict)))
    spec-dict))

(defn list-policies
  [oauth-token]
  (let [response  (http/get
                   (util/join-url server-address "/policies")
                   {:oauth-token oauth-token})
        spec-dict (spec-convert-policy-list (:body response))]
    (when-not (s/valid? ::hspec/list-policies-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/list-poilicies-response spec-dict)))
    spec-dict))

(defn list-clients
  [oauth-token]
  (let [response  (http/get
                   (util/join-url server-address "/clients")
                   {:oauth-token oauth-token})
        spec-dict (spec-convert-client-list (:body response))]
    (when-not (s/valid? ::hspec/list-clients-response spec-dict)
      (incorrect-response-exception (s/explain-str ::hspec/list-clients-response spec-dict)))
    spec-dict))

(defn delete-client!
  [client-id oauth-token]
  (let [url        (util/join-url server-address (str "/clients/" (url-encode client-id)))
        response   (http/delete url
                                {:content-type :json
                                 :oauth-token oauth-token})
        body       (:body response)]
    (when-not (nil? body)
      (incorrect-response-exception (str "DELETE returned" body ", nil expected")))))

(defn delete-policy!
  [policy-id oauth-token]
  (let [url      (util/join-url server-address (str "/policies/" (url-encode policy-id)))
        response (http/delete url {:content-type :json
                                   :oauth-token oauth-token})
        body     (:body response)]
    (when-not (nil? body)
      (incorrect-response-exception (str "DELETE returned" body ", nil expected")))))
