FROM clojure:lein-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

RUN apk add --no-cache nodejs python-dev build-base npm bash yarn
RUN mkdir /app
WORKDIR /app
COPY .yarnrc project.clj .babelrc package.json webpack.config.js yarn.lock \
     /app/
COPY lib/ /app/lib/
RUN lein do with-profile dev deps
RUN yarn install --frozen-lockfile --non-interactive
COPY resources /app/resources/
COPY test /app/test/
COPY src /app/src/
RUN NODE_ENV=production lein build-with-webpack

ENTRYPOINT ["java", "-jar", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "/app/target/enki.jar"]
