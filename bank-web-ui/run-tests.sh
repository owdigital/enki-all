#!/usr/bin/env bash

set -e -o pipefail -o nounset

NODE_ENV=test yarn
NODE_ENV=test yarn unitTest
NODE_ENV=test yarn integrationTest