#!/bin/bash

##  Expected environment variables:
#       PARTNER_ID - ID of Enki partner to be added to the Enki Core Hydra
#       PARTNER_SECRET - Secret of Enki partner to be added to the Enki Core Hydra
#       PARTNER_CALLBACK - Callback URIs for the Enki partner
#       PARTNER_INSTANCE - Address of the partner's Hydra instance
#       DOMAIN_NAME - Partner's domain name
#       CORE_HYDRA_ADDR - Address of the Enki Core Hydra instance
#       ENKI_ONBOARD_ID - ID of the client to be used to onboard the partner to Enki
#       ENKI_ONBOARD_SECRET - Secret of the client to be used to onboard the partner to Enki
#       ENKI_CONSUMER_ID - ID of the Enki consumer client
#       ENKI_CONSUMER_SECRET - Secret of the Enki consumer client
#       ENKI_CONSUMER_CALLBACKS - Callback URIs for Enki

set -x
set -e -o pipefail -o nounset

function run_until_up() {
  # Wait until the service is up
  # Sadly, I can't just use alarm(2) here.
  local url=$1
  local parent_pid=$$
  (sleep 120; echo "Healthcheck timed out!"; kill -TERM $parent_pid) &
  local timeout_pid=$!
  until [[ "$(curl -s -o /dev/null -w %{http_code} $url)" = "200" ]]; do
    echo "checking url $url" $(curl -s -o /dev/null -w %{http_code} $url)
    sleep 1
  done
  kill "$timeout_pid"
}


PATH=$PATH:/go/bin

echo "Using instance: $PARTNER_INSTANCE for $DOMAIN_NAME"

# Configure the Partner's database.
hydra migrate sql "$DATABASE_URL"

run_until_up $PARTNER_INSTANCE/health

hydra connect --url "$PARTNER_INSTANCE" --id "admin" --secret "demo-password"

if ! hydra policies get openid-id_token-policy; then
  hydra policies create \
      --skip-tls-verify \
      --actions get \
      --description "Allow everyone to read the OpenID Connect ID Token public key" \
      --allow \
      --id openid-id_token-policy \
      --resources rn:hydra:keys:hydra.openid.id-token:public \
      --subjects "<.*>"
fi

if ! hydra policies get public-key-policy; then
  hydra policies create \
      --skip-tls-verify \
      --actions get \
      --description "Allow everyone to read the public keys" \
      --allow \
      --id public-key-policy \
      --resources "rn:hydra:keys:<[^:]+>:public" \
      --subjects "<.*>"
fi

## Create the consent app

if ! hydra clients get consent-app; then
  hydra clients create \
    --skip-tls-verify \
    --id consent-app \
    --secret consent-secret \
    --name "Bank Consent App Client" \
    --grant-types client_credentials \
    --response-types token \
    --allowed-scopes hydra.keys.get
fi

if ! hydra policies get consent-app-policy; then
  hydra policies create \
    --skip-tls-verify \
    --actions get \
    --allow \
    --id consent-app-policy \
    --resources "rn:hydra:keys:hydra.consent.<.*>" \
    --subjects consent-app
fi

# Register Enki Consumer
if ! hydra clients get "${ENKI_CONSUMER_ID}"; then
  hydra clients create \
      --skip-tls-verify \
      --id "${ENKI_CONSUMER_ID}" \
      --secret "${ENKI_CONSUMER_SECRET}" \
      --grant-types authorization_code,refresh_token,client_credentials,implicit \
      --response-types token,code,id_token \
      --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,user:uid \
      --callbacks "\"${ENKI_CONSUMER_CALLBACKS}\""
fi

echo "Partner Hydra instance setup complete. Beginning registration with Core..."

run_until_up $CORE_HYDRA_ADDR/health
echo "Connecting to Core Hydra at $CORE_HYDRA_ADDR"

hydra connect --url "$CORE_HYDRA_ADDR" --id "$ENKI_ONBOARD_ID" --secret "$ENKI_ONBOARD_SECRET"

if ! hydra clients get "${PARTNER_ID}"; then
  hydra clients create \
      --skip-tls-verify \
      --id "${PARTNER_ID}" \
      --secret "${PARTNER_SECRET}" \
      --grant-types authorization_code,refresh_token,client_credentials,implicit \
      --response-types token,code,id_token \
      --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,firstName,lastName,email,streetName,phoneNumber,streetNumber,city,country,birthdate,nationality,docno,documentType,zipCode,addressNumber,residenceAddress,province,gender,birthPlace \
      --callbacks "\"${PARTNER_CALLBACKS}\""
fi
