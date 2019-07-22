FROM oryd/hydra:v0.9.16-without-telemetry

RUN apk add --upgrade --no-cache bash curl python3
RUN mkdir -vp /srv
COPY configure-core-hydra.sh /srv/configure-core-hydra.sh
COPY setup-readiness-check.sh /srv/setup-readiness-check.sh
