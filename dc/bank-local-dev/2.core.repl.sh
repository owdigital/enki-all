#!/usr/bin/env bash

export BASE_URI_BANK_A=http://localhost:9001
export BASE_URI_BANK_B=http://localhost:9002
export DATABASE_URL=jdbc:postgresql://localhost:5432/enki-core?user=postgres&password=postgres
export HYDRA_SERVER_URL=http://localhost:5444
export EXTERNAL_HYDRA_SERVER_URL=http://localhost:5444
export HTTP_PORT=3000
export HYDRA_ADMIN_LOGIN=admin
export HYDRA_ADMIN_PASSWORD=demo-password
export HYDRA_CLIENT_ID=consent-app
export HYDRA_CLIENT_SECRET=consent-secret
export OAUTH_BASE_URI_BANK_A=http://localhost:4444
export OAUTH_AUTHORIZE_URI_BANK_A=http://localhost:4444/oauth2/auth
export OAUTH_CLIENT_ID_BANK_A=enki-consumer
export OAUTH_CLIENT_SECRET_BANK_A=enki-secret
export OAUTH_BASE_URI_BANK_B=http://localhost:4445
export OAUTH_AUTHORIZE_URI_BANK_B=http://localhost:4445/oauth2/auth
export OAUTH_CLIENT_ID_BANK_B=enki-consumer
export OAUTH_CLIENT_SECRET_BANK_B=enki-secret
export OAUTH_BASE_URI_IRON_BANK=http://localhost:4446
export OAUTH_AUTHORIZE_URI_BANK_IRON_BANK=http://localhost:4446/oauth2/auth
export OAUTH_CLIENT_ID_IRON_BANK=enki-consumer
export OAUTH_CLIENT_SECRET_IRON_BANK=enki-secret
# From dc/enki.yaml's enki service's env variables.

export HOSTNAME=`hostname`

cd core
screen -dm -S core-repl bash -c 'lein do clean, repl;sleep 15'

(
sleep 30
screen -X -S core-repl stuff '(repl/go)'
screen -X -S core-repl stuff $'\n'
) &
