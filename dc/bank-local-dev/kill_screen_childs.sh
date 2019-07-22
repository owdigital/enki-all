#!/usr/bin/env bash

set -x

screen -X -S core-repl stuff 'quit'
screen -X -S core-repl stuff $'\n'

# @todo These are a little flaky...
screen -X -S webpack-watch stuff $'\003'
screen -X -S agent-repl stuff 'quit'
screen -X -S agent-repl stuff $'\n'
screen -X -S webpack-watch stuff $'\003'
