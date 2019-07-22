#!/usr/bin/env bash

export ENKI_SERVER_URL=http://localhost:3000/
export BANK_NAME=bank-a
export OAUTH_CLIENT_ID=bank-a-client
export CONSUS_USER=test-agent1@test.labshift.io
export AGENT_URL=http://localhost:3010/
export UPSPINSERVER_FQDN=localhost
export UPSPINSERVER_PORT=8090
export CONSUS_CONFIG=${CONSUS_CONFIG:-$HOME/upspin}
# From dc/enki.yaml's agent-a service's env variables.

cd agent
screen -dm -S agent-repl bash -c 'lein repl;sleep 15'

(
sleep 20
screen -X -S agent-repl stuff "(require '[enki-agent.core :as core])"
screen -X -S agent-repl stuff $'\n'
screen -X -S agent-repl stuff '(core/-main "new-signing-key" "sign-a.key")'
screen -X -S agent-repl stuff $'\n'
screen -X -S agent-repl stuff '(core/-main "agent" "sign-a.key")'
screen -X -S agent-repl stuff $'\n'
) &