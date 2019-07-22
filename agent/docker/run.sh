#!/bin/bash

function runme() {
  java -jar -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap /srv/target/uberjar/enki-agent.jar "$@"
}

set -x
set -e -o pipefail -o nounset

AGENT=yes

while getopts n option
do
 case "${option}"
 in
 n) AGENT=no;
 esac
done

SIGNING_KEY=sign.key
if [ ! -f "$SIGNING_KEY" ]; then
  runme new-signing-key "$SIGNING_KEY"
fi

if [ "$AGENT" = "yes" ]; then
  runme agent "$SIGNING_KEY"
fi
