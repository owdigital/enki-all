#!/bin/bash

set -x
set -e -o pipefail -o nounset

function run_until_up() {
  # Wait until the service is up
  # Sadly, I can't just use alarm(2) here.
  local url=$1
  local parent_pid=$$
  (sleep 60; echo "Healthcheck timed out!"; kill -TERM $parent_pid) &
  local timeout_pid=$!
  until [[ "$(curl -s -o /dev/null -w %{http_code} $url)" = "200" ]]; do
    sleep 1
  done
  kill "$timeout_pid"
}


INSTANCE=${INSTANCE:-http://hydra}
BANK_A_CALLBACK=${BANK_A_CALLBACK:-https://bank-a.$DOMAIN_NAME/signupcallback}
BANK_B_CALLBACK=${BANK_B_CALLBACK:-https://bank-b.$DOMAIN_NAME/signupcallback}
PATH=$PATH:/go/bin

echo "Using instance: $INSTANCE for $DOMAIN_NAME"

# Configure the database.
hydra migrate sql "$DATABASE_URL"

run_until_up $INSTANCE/health

hydra connect --url "$INSTANCE" --id "admin" --secret "demo-password"

# Register Bank A Consumer
if ! hydra clients get bank-a-client; then
  hydra clients create \
      --skip-tls-verify \
      --id bank-a-client \
      --secret bank-a-secret \
      --grant-types authorization_code,refresh_token,client_credentials,implicit \
      --response-types token,code,id_token \
      --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,firstName,lastName,email,streetName,phoneNumber,streetNumber,city,country,birthdate,nationality,docno,documentType,zipCode,addressNumber,residenceAddress,province,gender,birthPlace \
      --callbacks $BANK_A_CALLBACK
fi

# Register Bank B Consumer
if ! hydra clients get bank-b-client; then
  hydra clients create \
      --skip-tls-verify \
      --id bank-b-client \
      --secret bank-b-secret \
      --grant-types authorization_code,refresh_token,client_credentials,implicit \
      --response-types token,code,id_token \
      --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,firstName,lastName,email,streetName,phoneNumber,streetNumber,city,country,birthdate,nationality,docno,documentType,zipCode,addressNumber,residenceAddress,province,gender,birthPlace \
      --callbacks $BANK_B_CALLBACK
fi

hydra policies create \
    --skip-tls-verify \
    --actions get \
    --description "Allow everyone to read the OpenID Connect ID Token public key" \
    --allow \
    --id openid-id_token-policy \
    --resources rn:hydra:keys:hydra.openid.id-token:public \
    --subjects "<.*>"

hydra policies create \
    --skip-tls-verify \
    --actions get \
    --description "Allow everyone to read the public keys" \
    --allow \
    --id public-key-policy \
    --resources "rn:hydra:keys:<[^:]+>:public" \
    --subjects "<.*>"

## Create the consent app

if ! hydra clients get consent-app; then
  hydra clients create \
    --skip-tls-verify \
    --id consent-app \
    --secret consent-secret \
    --name "ENKI Consent App Client" \
    --grant-types client_credentials \
    --response-types token \
    --allowed-scopes hydra.keys.get
fi

hydra policies create \
  --skip-tls-verify \
  --actions get \
  --allow \
  --id consent-app-policy \
  --resources "rn:hydra:keys:hydra.consent.<.*>" \
  --subjects consent-app