FROM node:12-alpine
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

RUN apk add --no-cache python-dev build-base sqlite git
WORKDIR /app
COPY .yarnrc .babelrc package.json config.js sensitive-config-values.json \
     .istanbul.yml webpack.config.js yarn.lock \
     /app/
RUN yarn install --prefer-offline --force --frozen-lockfile --non-interactive
COPY server.js /app
COPY client/ /app/client/
COPY server/ /app/server/
COPY shared/ /app/shared/
ENV NODE_ENV=production
ARG BANK_THEME=bank-default
RUN yarn run webpack --env.BANK_THEME=$BANK_THEME
WORKDIR /app
ENTRYPOINT yarn start
