FROM clojure:lein-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

RUN apk add --no-cache bash
COPY project.clj /srv/
COPY lib/ /srv/lib/
WORKDIR /srv
RUN lein deps
COPY src/ /srv/src
COPY resources/ /srv/resources/
RUN lein uberjar

COPY /test/wait-for-it.sh /srv/wait-for-it
RUN chmod +x /srv/wait-for-it
COPY /docker/run.sh /test/configure-upspin.sh /srv/
RUN chmod +x /srv/configure-upspin.sh
COPY docker/upspin /usr/local/bin/upspin
RUN chmod +x /usr/local/bin/upspin
EXPOSE 3010
ENTRYPOINT ["/srv/run.sh"]
