FROM clojure:lein-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

COPY project.clj /app/
WORKDIR /app
RUN lein deps
COPY src bletchley /app/
RUN lein install