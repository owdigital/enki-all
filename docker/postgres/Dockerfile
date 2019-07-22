FROM postgres:9.6-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

RUN apk add --no-cache --repository http://dl-3.alpinelinux.org/alpine/edge/testing/ --allow-untrusted gosu

COPY docker-init.sh /usr/local/bin/
COPY docker-entrypoint.sh /usr/local/bin/

# From https://github.com/mrts/docker-postgresql-multiple-databases
COPY create-multiple-dbs.sh /docker-entrypoint-initdb.d/

# Add UUID support https://www.postgresql.org/docs/current/static/uuid-ossp.html
COPY install-uuid-extension.sql /docker-entrypoint-initdb.d/