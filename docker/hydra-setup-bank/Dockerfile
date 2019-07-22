FROM oryd/hydra:v0.9.16-without-telemetry

RUN apk add --upgrade --no-cache bash python3 curl
RUN mkdir -vp /srv
COPY configure-bank-hydra.sh /srv/configure-bank-hydra.sh
COPY setup-readiness-check.sh /srv/setup-readiness-check.sh
