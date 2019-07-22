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
PATH=$PATH:/go/bin

echo "Using instance: $INSTANCE for $DOMAIN_NAME"

# Configure the database.
hydra migrate sql "$DATABASE_URL"

run_until_up $INSTANCE/health

hydra connect --url "$INSTANCE" --id "admin" --secret "demo-password"

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
