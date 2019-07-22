#!/usr/bin/env bash

set -eux -o pipefail

CONSUS_USER=${CONSUS_USER:-test-agent@test.labshift.io}
CONSUS_CONFIG=${CONSUS_CONFIG:-$HOME/upspin}

# From https://unix.stackexchange.com/a/137639/73838
function fail {
  echo "$1" >&2
  exit 1
}

function retry {
  local n=1
  local max=6
  local delay=10
  while true; do
    if "$@"; then
      break
    fi

    if [[ $n -lt $max ]]; then
      n=$[$n+1]
      echo "Command failed. Attempt $n/$max:"
      sleep $delay;
    else
      fail "The command has failed after $n attempts."
    fi
  done
}

function do_once() {
  local flagfile=$1
  shift
  if [ ! -f "$flagfile" ]; then
    if $@; then
      touch "$flagfile"
    else
      return $?
    fi
  fi
}

if [ ! -f $CONSUS_CONFIG/config ]; then
    do_once create-config.flag upspin -config=$CONSUS_CONFIG/config signup -server=$UPSPINSERVER_FQDN:$UPSPINSERVER_PORT -configonly $CONSUS_USER
    sed -i"" "s/keyserver: .*/keyserver: remote,keyserver:8070/" $CONSUS_CONFIG/config
    echo "secrets: $CONSUS_CONFIG/$CONSUS_USER" >> $CONSUS_CONFIG/config
    cat $CONSUS_CONFIG/config
    mv -v $HOME/.ssh/* $CONSUS_CONFIG/
    do_once signup.flag upspin -config=$CONSUS_CONFIG/config signup -signuponly

    # upspinserver might well have not finished booting yet, so retry a few times
    retry upspin -config=$CONSUS_CONFIG/config mkdir $CONSUS_USER
fi
