#!/usr/bin/env bash
set -e

if [ "${1:0:1}" = '-' ]; then
	set -- postgres "$@"
fi

# allow the container to be started with `--user`
if [ "$1" = 'postgres' ] && [ "$(id -u)" = '0' ]; then
	exec gosu postgres "$BASH_SOURCE" "$@"
fi

exec "$@"