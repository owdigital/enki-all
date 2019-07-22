#!/bin/sh

set -e -o nounset -o pipefail

cd "$(mktemp -d /tmp/XXXXXXXX)"
echo running python server
python3 -m http.server
