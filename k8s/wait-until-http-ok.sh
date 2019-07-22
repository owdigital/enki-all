#!/bin/bash

set -e -o pipefail -o nounset

HTTP_TIMEOUT=${HTTP_TIMEOUT:-60}

trap 'echo "Timed out (probably)!"; exit 1' TERM

function run_until_up() {
  # Wait until the service is up
  # Sadly, I can't just use alarm(2) here.
  local url=$1
  until [[ "$(curl -s -o /dev/null -w %{http_code} $url)" = "200" ]]; do
    sleep 1
  done
}

URL=$1

if [ -z "${__WITH_TIMEOUT-}" ]; then
  __WITH_TIMEOUT=t exec timeout "$HTTP_TIMEOUT" "$0" "$@"
fi

echo "Checking resource: $URL"

run_until_up $URL
