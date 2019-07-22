#!/usr/bin/env bash

cd core
screen -dm -S webpack-watch bash -c 'yarn webpack-watch;sleep 15'
