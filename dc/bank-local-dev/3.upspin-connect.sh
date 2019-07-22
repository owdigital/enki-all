#!/usr/bin/env bash

# Based on: agent/test/configure-upspin.sh

set -eux -o pipefail

export CONSUS_USER=test-agent1@test.labshift.io
export CONSUS_CONFIG=${CONSUS_CONFIG:-$HOME/upspin}
export UPSPINSERVER_FQDN=localhost
export UPSPINSERVER_PORT=8090

mv $CONSUS_CONFIG $CONSUS_CONFIG.$(date +%Y-%m-%d-%H-%M-%S).bak
mkdir -p $CONSUS_CONFIG

upspin -config=$CONSUS_CONFIG/config signup \
    -configonly -server=$UPSPINSERVER_FQDN:$UPSPINSERVER_PORT $CONSUS_USER
sed -i"xxx" "s/keyserver: .*/keyserver: remote,localhost:8070/" $CONSUS_CONFIG/config
echo "secrets: $CONSUS_CONFIG/$CONSUS_USER" >> $CONSUS_CONFIG/config
cat $CONSUS_CONFIG/config
mv -v $HOME/.ssh/$CONSUS_USER $CONSUS_CONFIG/
upspin -config=$CONSUS_CONFIG/config signup -signuponly

upspin -config=$CONSUS_CONFIG/config mkdir $CONSUS_USER
