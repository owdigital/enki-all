#!/usr/bin/env bash

set -e
set -u

function create_database() {
    local database=$1
    echo "  Creating database '$database'"
    ndbs=$(echo "select count(d.datname) FROM pg_catalog.pg_database d where d.datname = :'db'" | psql -U "$POSTGRES_USER" -A -t -v "db=$database" template1 -f - )

    if [ $ndbs -eq 0 ]; then
      createdb -O $POSTGRES_USER $database
    fi
}

if [ ! -z ${POSTGRES_MULTIPLE_DATABASES:-} ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        create_database $db
    done
    echo "Multiple databases created"
fi
