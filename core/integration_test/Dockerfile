FROM clojure:lein-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

COPY integration_test/wait-for-it.sh /srv/wait-for-it.sh

COPY . /app
WORKDIR /app
RUN lein with-profile dev deps

ENTRYPOINT /srv/wait-for-it.sh --strict --timeout=0 hydra:4444 && lein tests :all