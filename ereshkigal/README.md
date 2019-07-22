# Ereshkigal

A suite of end to end smoke tests, to verify the correctness of deployments.

Named after the underworld deity who determines that Inanna should pass through seven gates before being granted access to the underworld. 

## Prerequisites

Ereshkigal uses chromedriver to automate Chrome. 

MacOS:
```
brew install chromedriver
```

## Build and run

Ereshkigal is mostly designed to run from Jenkins against a staging environment. The simplest way to use it is:

```sh
make check
```
This will fetch the required python dependencies, and run the suite in py.test.

To run against a non-staging instance (say, your development branch), run:

```sh
make check ENKI_SERVER_URL=http://localhost:3000 CORE_HYDRA_URL=http://localhost:4444 BANK_URLS="http://localhost:9001 http://localhost:9002"
```

## Demo Mode

If you set the environment variable `DEMO_MODE` to some integer number of seconds, the tests will run non-headless and pause for that many seconds. This is useful for being able to visualise what's going on.

## Screenshots

If you set the environment variable `SCREENSHOT_PATH`, screenshots of the webpage before and after each click will be produced in `SCREENSHOT_PATH` folder.

