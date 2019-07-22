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
(ns enki-core.hydra.consent
  "Implements necessary steps for the bank to obtain consent on pieces of user's PII
   which involves verifying the consent challenge signed by Hydra and responding to it.

   Verifies client's request for scopes: authorization only succeeds if the scopes
   the client requested are a subset of allowed scopes.
   (done by resolve-consent). Allowed scopes are specified when consumer app is created through API."
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as buddy-keys]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [enki-core.hydra.api :as api]
            [enki-core.hydra.spec :as hspec]
            [enki-core.util :as util]
            [environ.core :refer [env]]
            [ring.util.response :as resp]
            [ring.util.codec :refer [url-encode]])
  (:import (com.nimbusds.jose.jwk RSAKey)
           (org.bouncycastle.openssl.jcajce JcaPEMWriter)
           (java.io StringWriter)))

(def hydra-server-url (env :hydra-server-url))
(def external-hydra-server-url (or
    (env :external-hydra-server-url)
    hydra-server-url)) ; default to the standard url
(def hydra-auth {:token-path "/oauth2/token"
                 :keys-path "/keys"})

(defn time-now
  []
  (System/currentTimeMillis))

(defn get-keys-token
  [client-id client-secret]
  (let [form-params {:grant_type "client_credentials"
                     :scope "hydra.keys.get"
                     :client_id client-id
                     :client_secret client-secret}
        endpoint    (:token-path hydra-auth)
        body        (api/post-request-credentials
                     form-params
                     endpoint
                     false
                     client-id
                     client-secret)]
    (when-not (s/valid? ::hspec/token-response body)
      (api/incorrect-response-exception (s/explain ::hspec/token-response body)))
    (let [token       (get body ::hspec/access_token)
          expires-in  (long (get body ::hspec/expires_in))
          expires-at  (+ (time-now) expires-in)]
      {:token token
       :expires-at expires-at})))

(defprotocol IToken
  (get-value [this] "Get a token. Refreshes one if the old one has expired"))

(defrecord HydraToken [client-id client-secret token-data]
  IToken

  (get-value [this]
    (when (> (time-now) (:expires-at @token-data))
      (reset! token-data (get-keys-token client-id client-secret)))
    @token-data))

(defn token-instance
  [client-id client-secret]
  (map->HydraToken {:token-data (atom {:token nil :expires-at 0})
                    :client-id client-id
                    :client-secret client-secret}))

(defn- generate-string
  ^String
  [xs]
  (json/generate-string xs {:key-fn name}))

(defn pub-jwk->pub-key
  [jwk]
  (let [public-key  (.toRSAPublicKey (RSAKey/parse (generate-string jwk)))]
    public-key))

(defn priv-jwk->pem-key
  [jwk]
  (let [writer      (StringWriter.)
        priv-key    (.toRSAPrivateKey (RSAKey/parse (generate-string jwk)))
        pem-writer  (JcaPEMWriter. writer)]
    (.writeObject pem-writer priv-key)
    (.flush pem-writer)
    (buddy-keys/str->private-key (str writer))))

(defn get-key
  [token-data set-id k-id]
  (let [url  (util/join-url hydra-server-url
                            (format "%s/%s/%s"
                                    (:keys-path hydra-auth)
                                    set-id
                                    k-id))
        res  (client/get url {:oauth-token (:token token-data)})
        body (api/spec-convert-response (:body res))]
    (when-not (s/valid? ::hspec/get-keys-response body)
      (api/incorrect-response-exception (s/explain-str ::hspec/get-keys-response res)))
    (first (::hspec/keys body))))

(defn verify-consent-challenge
  [token-holder challenge]
  (let [key      (get-key (get-value token-holder) "hydra.consent.challenge" "public")
        pub-key  (pub-jwk->pub-key key)]
    (update-in
      (jwt/unsign challenge pub-key {:alg :rs256})
      [:redir] ; This needs rewriting to cope with the external name of Hydra being different to the internal reference
      #(clojure.string/replace % hydra-server-url external-hydra-server-url))))

(defn generate-consent-response
  [token-holder decoded-challenge subject scopes at idt]
  (let [{:keys [aud exp jti]}  decoded-challenge
        ky                     (get-key (get-value token-holder) "hydra.consent.response" "private")
        priv-key               (-> (assoc ky :dp "" :dq "" :qi "")
                                   priv-jwk->pem-key)
        data                   {:jti jti
                                :aud aud
                                :exp exp
                                :scp scopes
                                :sub subject
                                :at_ext at
                                :id_ext idt}]
    (jwt/sign data priv-key {:alg :rs256})))

(defn resolve-consent
  [token-holder decoded-challenge scopes user extra-data]
  (let [data               (select-keys user [:name])
        consent            (generate-consent-response
                            token-holder
                            decoded-challenge
                            (str (:id user))
                            scopes
                            extra-data
                            data)]
    (resp/redirect (str (:redir decoded-challenge)
                        "&consent="
                        (url-encode consent)))))

(defn get-auth-token
  [consumer-id consumer-secret code redirect-uri]
  (let [res   (client/post (util/join-url hydra-server-url (:token-path hydra-auth))
                           {:basic-auth [consumer-id consumer-secret]
                            :form-params {:code code
                                          :grant_type "authorization_code"
                                          :response_type "code"
                                          :redirect_uri redirect-uri}})
        body   (api/spec-convert-response (:body res))]
    (when-not (s/valid? ::hspec/token-response body)
      (api/incorrect-response-exception (s/explain ::hspec/token-response body)))
    (let [token       (get body ::hspec/access_token)
          expires-in  (long (get body ::hspec/expires_in))
          expires-at  (+ (time-now) expires-in)]
      {:token token
       :expires-at expires-at})))

(defn validate-token
  [token-holder to-validate]
  (let [{token :token}  (get-value token-holder)
        url             (util/join-url hydra-server-url "/oauth2/introspect")
        res             (client/post url
                                     {:oauth-token token
                                      :form-params {:token to-validate}})]
    (json/parse-string (:body res) true)))

(defn- get-jwks-uri
  [profile]
  (if-let [conf (try
                  (client/get (util/join-url (:base-uri profile) "/.well-known/openid-configuration"))
                  (catch Exception e nil))]
    (-> (:body conf) (json/parse-string true) (:jwks_uri))
    (util/join-url (:base-uri profile) "/.well-known/jwks.json")))

(defn- get-jwk-pub-key
  ([token]
   (let [response  (client/get (util/join-url hydra-server-url ".well-known/jwks.json")
                               {:oauth-token token})
         body      (json/parse-string (:body response) true)]
     (pub-jwk->pub-key (first (:keys body)))))
  ([token profile]
   (let [response (client/get (get-jwks-uri profile) {:oauth-token token})
         body     (json/parse-string (:body response) true)]
     (pub-jwk->pub-key (first (:keys body))))))

(defn get-unsigned-id-token
  "Takes in ::hspec/token-response and unsigns `id_token` using Hydra's pub key"
  [token-data oauth-token]
  (let [pub-key   (get-jwk-pub-key oauth-token)]
    (jwt/unsign (::hspec/id_token token-data) pub-key {:alg :rs256})))

(defn get-verified-id-token
  "Retrieve `id_token` from an access-token and verify it"
  [access-token profile]
  (let [pub-key (get-jwk-pub-key (:token access-token) profile)]
    ;; TODO: check the alg in the id-token
    (jwt/unsign (:id-token access-token) pub-key {:alg :rs256})))
