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
(ns enki-core.web.oidc
  "Implements the linking of the enki account with bank accounts. This is
  needed for the enki user to find existing metadata about her."
  (:require [clj-http.client :as http]
            [clj-time.core :as time]
            [clojure.string :as str]
            [compojure.core :refer [GET routes]]
            [crypto.random :as random]
            [enki-core.hydra.consent :as oauth] ; FIXME: extract generic oauth code from consent
            [enki-core.store.component :refer [with-transaction]]
            [enki-core.store.core :as store]
            [enki-core.util :as util]
            [ring.util.codec :as codec]
            [ring.util.request :as req]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log])
  (:import (java.net URI)))

;; ;; Make sure enki is registered on bank A hydra:
;; hydra clients create --skip-tls-verify \
;;   --name "Enki Consumer Client" \
;;   --id enki-consumer \
;;   --secret enki-secret \
;;   --grant-types authorization_code,refresh_token,client_credentials,implicit \
;;   --response-types token,code,id_token \
;;   --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,user:uid \
;;   --callbacks 'http://localhost:3000/linkacc/bank-a/callback'  # MUST MATCH !!
;; ;; FIXME: fetch from db
(def oauth-profiles
  (into {} (map (fn [bank]
                  (let [base-uri  (:oauth-base-uri bank)
                        bank-name (:name bank)]
                    [bank-name
                     {:base-uri         base-uri
                      :authorize-uri    (:oauth-authorize-uri bank)
                      :access-token-uri (str base-uri "/oauth2/token")
                      :client-id        (:oauth-client-id bank)
                      :client-secret    (:oauth-client-secret bank)
                      :scopes           ["openid"]
                      :redirect-uri     (str "/linkacc/" (codec/url-encode bank-name) "/callback")
                      :landing-uri      "/"
                      :basic-auth?      true}]))
                (util/get-banks-from-env))))

;; This code is mostly lifted from ring-oauth2 which could indeed provide us
;; with an id_token. Unfortunately it's too rigid for our use case: 1. make the
;; our endpoints dynamic, 2. actually link enki and bank accounts after the
;; OIDC call.


(defn- redirect-uri [profile request]
  (-> (req/request-url request)
      (URI/create)
      (.resolve (:redirect-uri profile))
      str))

(defn- scopes [profile]
  (str/join " " (map name (:scopes profile))))

(defn- authorize-uri [profile request state]
  (str (:authorize-uri profile)
       (if (.contains ^String (:authorize-uri profile) "?") "&" "?")
       (codec/form-encode {:response_type "code"
                           :client_id     (:client-id profile)
                           :redirect_uri  (redirect-uri profile request)
                           :scope         (scopes profile)
                           :state         state})))

(defn- random-state []
  (-> (random/base64 9) (str/replace "+" "-") (str/replace "/" "_")))

(defn- state-matches? [request]
  (= (get-in request [:session ::state])
     (get-in request [:query-params "state"])))

(defn- format-access-token
  [{{:keys [access_token expires_in refresh_token id_token]} :body :as r}]
  (-> {:token access_token}
      (cond-> expires_in (assoc :expires (-> expires_in time/seconds time/from-now))
              refresh_token (assoc :refresh-token refresh_token)
              id_token (assoc :id-token id_token))))

(defn- request-params [profile request]
  {:grant_type    "authorization_code"
   :code          (get-in request [:query-params "code"])
   :redirect_uri  (redirect-uri profile request)})

(defn- add-header-credentials [opts id secret]
  (assoc opts :basic-auth [id secret]))

(defn- add-form-credentials [opts id secret]
  (assoc opts :form-params (-> (:form-params opts)
                               (merge {:client_id     id
                                       :client_secret secret}))))

;; Here is an access-token example:
;; {:token
;; "8N3UZ5lSan6y4Tr6M5IxOtDcC7ZJF3AdSot5acRkyrc.1VHB2nA26A_TaF66KDww7a0atKGB6ZwCvoptrSlnHsY",
;; :expires #object[org.joda.time.DateTime 0x41ab5199
;; "2017-11-29T12:30:54.984Z"], :id-token "eyJhbGciOiJSUzI1NiIsImtpZCI6InB1YmxpYyIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJlbmtpLWNvbnN1bWVyIiwiYXV0aF90aW1lIjoxNTExOTU1MDU1LCJleHAiOjE1MTE5NTg2NTUsImlhdCI6MTUxMTk1NTA1NSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo0NDQ0Iiwibm9uY2UiOiIiLCJzdWIiOiJ1c2VyOjE6YWxpY2UifQ.hDaImmpQfhJkSvfijd9bJqscj4o9kQvGy9padUsmnc7zfGQL4hlaHCBjEzOwFjmyNlTDTU8XqdTFf92o1ckwPAR824j44d0BEH5w5CouSsnygSctBjRn24ORfSh7iQIcB6bglsi--o7YCEf9zA0rd1xQgi460IBZ73jM23nYsoa3O8koTIXgn6VnBQcFji9-RoLOIW_ODjQTed8MoRmyL2Dyg8_Ts7yTg6alWQ1UJmVvwi4MMVOeIbJlGTVicEtpQkb1tbFHY59LJTM5BO24MweEHbp7Rmhz596UX4cQBhqA3DbW_LdQmo5nQVEKJbEQCQqp9ongn-QL-YPX9mbGPfyteuM3aHfk-SKiP7sBQNhGdNwgfeLAiTzHV3rCzxSDcDxTZoNvP0sLx2WE5HOQN4OgHn-tDgDIvRid1AuJtIzeluQlGS3oReK5Qa7ne3bW3JfsCqn5jmF23yJHEOd0O5VqlYLRb5Un0e0oo41wQHsVZyTLTlEBh-FNreageswCgkVsIwFnRef-aRXiEZ2S3NSCpeOqhkgbXXY31D59z88K_omq_XMuRCCwMH-7avZjeT0h8zkSMY1KRCxSYJ4arFnNtbwlqYje9ieT_4xrNcqbXtDyF22dh7ipwcfDTHWy-AsIV82EkEDjRWObCgNIH-ImeNrnFQA9BGdXLxI6fFE"}
(defn- get-access-token
  [{:keys [access-token-uri client-id client-secret basic-auth?]
    :or {basic-auth? false} :as profile} request]
  (let [data (cond-> {:accept :json, :as  :json,
              :form-params (request-params profile request)}
              basic-auth? (add-header-credentials client-id client-secret)
              (not basic-auth?) (add-form-credentials client-id client-secret))]
    (if (nil? (-> data :form-params :code))
      {:err {:status 400 :body "Missing 'code' parameter for access token"}}
      (do
        (log/info "Getting access token from" access-token-uri data)
        (format-access-token (http/post access-token-uri data))))))

(defn state-mismatch-handler [_]
  {:status 400, :headers {}, :body "State mismatch"})

(defn oidc-routes
  [{:keys [db] :as components}]
  (routes
    (GET "/linkacc/:bank-name" [bank-name :as request]
      (let [session (:session request)
            profile (oauth-profiles bank-name)
            state   (random-state)]
            ;; FIXME: check bank-name exists
        (-> (resp/redirect (authorize-uri profile request state))
            (assoc :session (assoc session ::state state)))))

    (GET "/linkacc/:bank-name/callback" [bank-name :as request]
      (if (contains? (:query-params request) "error")
        {:status 400 :body (str "Callback with error " (:query-params request))}
        (with-transaction db [tx {:read-only? false}]
          (let [session        (:session request)
                profile        (oauth-profiles bank-name)
                landing-uri    (:landing-uri profile)
                error-handler  (:state-mismatch-handler profile state-mismatch-handler)]
            (if (state-matches? request)
              (let [access-token (get-access-token profile request)]
                (if (contains? access-token :err)
                  (:err access-token)
                  (do
                    (if (:id-token access-token)
                      (let [id-token      (oauth/get-verified-id-token access-token profile)
                            bank-user-id  (:sub id-token)
                            user          (store/get-user-by-name db tx (:identity session) false)
                            bank          (store/get-bank-by-name db tx bank-name)]
                        ;; don't re-insert if we've already got an association
                        (when-not (store/get-user-association db tx (:id user) (:id bank))
                          (store/insert-user-association! db tx (:id user) (:id bank) bank-user-id))))
                    (-> (resp/redirect landing-uri)
                        (assoc :session (-> session
                                            (assoc-in [::access-tokens bank-name] access-token)
                                            (dissoc ::state)))))))
              (error-handler request))))))))
