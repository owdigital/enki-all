#!/bin/bash

if [ -z ${DOCKER} ]
then
        # Accommodates users on Windows Bash etc who need to call 'docker.exe'
        DOCKER=docker
        DOCKER_COMPOSE=docker-compose
fi 

while true; do
    containers=$($DOCKER ps --filter "network=dc_default" -q)
    if [ ! -z "$containers" ]; then
        break
    fi
    echo "Waiting for initial containers"
    sleep 5
done

while true; do
    $DOCKER inspect -f "{{.State.Health.Status}}" $($DOCKER ps --filter "network=dc_default" -q) | egrep 'starting|unhealthy' > /dev/null
    res=$?
    if [ $res -eq 1 ]; then
        break
    fi
    $DOCKER ps --filter "network=dc_default"
    echo ""
    sleep 5
done
$DOCKER ps --filter "network=dc_default"