#!/usr/bin/env bash

set -e -o pipefail -o nounset

if [ -z ${DOCKER} ]
then
	# Accommodates users on Windows Bash etc who need to call 'docker.exe'
	DOCKER=docker
	DOCKER_COMPOSE=docker-compose
fi

rm -rf target
rm -rf resources/public/build
lein with-profile dev deps
npm run webpack -- -p

$DOCKER_COMPOSE -f docker-compose-test.yml rm -v --force postgres
$DOCKER system prune --force

$DOCKER_COMPOSE -f docker-compose-test.yml up --build --abort-on-container-exit

npm run unitTest