# enki-bank-web-ui

## Overview

A sample bank web UI that demonstrates enki functionality

## Prerequisites

* Install [Node.js](https://nodejs.org/en/)
* Install [Yarn](https://yarnpkg.com/en/docs/install)
* `yarn install` will install the node dependencies

## Build and run

* `yarn start` will start the node server and serve the content at the address below
* Goto [http://localhost:9001/](http://localhost:9001/)

You can also override configuration settings with environment variables.
Examples:

```sh
BANK_NAME='Super Bank' DB__FILE=var/db_Super.sqlite yarn start
BANK_THEME='bank-b' yarn start
```

Once this bank is registered on enki, it's allocated a bank-id uuid which can
be passed to the bank-ui for the fake OIDC endpoint to work properly:

```sh
yarn run db:clean  # make sure the first user is created with id 1
BANK_THEME='bank-a' ENKI_CALLBACK_URL='http://localhost:3000/oidc-callback?uid=1&bank-id=<ENKI_PROVIDED_UUID>' yarn start
```

## Run tests locally

To run the tests, run:

```bash
./run-tests.sh
```


## Theming

The app supports theming; LESS styles can be added in `client/css/custom/`.

To add a new LESS style for `bank-x`, create a new folder `client/css/custom/bank-x` then copy and modify the LESS files from the `bank-default` folder. The custom styles can be in separate files, but need to contain variables named consistenly across all themes.

To start up with a specific theme, run 
```sh
BANK_THEME = 'bank-x' yarn start`
```
