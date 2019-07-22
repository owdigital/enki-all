#!/bin/bash

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

URL=$1

echo "Checking resource: $URL"

run_until_up $URL
