# Building and running Enki - general

There are three docker-compose files that enable the developer to work on any of the following three sets of services:

* Bank A or Bank B web-ui.
* Core or Agent.
* Everything.

The first two are designed for developer edit-and-reload workflow. The last is designed for running all the services as in a staging environment.

## Prerequisites

You should have the following installed:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [Leiningen](https://leiningen.org/)
* [Node.js](https://nodejs.org/) (8.x or 10.x)
* [Yarn](https://yarnpkg.com/en/)
* [Docker Compose](https://www.docker.com/community-edition)
* [Chromedriver](https://sites.google.com/a/chromium.org/chromedriver/)
* [Google cloud SDK](https://cloud.google.com/sdk/)

You need to be logged into the google container registry with `gcloud auth configure-docker`.


## 1. Working on bank-web-ui

Start core and agents for Bank A and Bank B by running:

```bash
make enki
```

It can take a few minutes for the services to launch.

To make sure that the services are running as expected, in another terminal, run:

```bash
make enki-status
```

A sample correct output is:

```
         Name                        Command               State                 Ports
----------------------------------------------------------------------------------------------------
dc_agent-a_1              bash -c /srv/wait-for-it - ...   Up       0.0.0.0:3010->3010/tcp
dc_agent-b_1              bash -c /srv/wait-for-it - ...   Up       3010/tcp, 0.0.0.0:3011->3011/tcp
dc_core_1                 bash -c wait-for-it hydra- ...   Up       0.0.0.0:3000->3000/tcp
dc_hydra-bank-a_1         sh -xc until /go/bin/hydra ...   Up       0.0.0.0:4444->4444/tcp
dc_hydra-bank-b_1         sh -xc until /go/bin/hydra ...   Up       0.0.0.0:4445->4444/tcp
dc_hydra-enki_1           sh -xc until /go/bin/hydra ...   Up       0.0.0.0:5444->4444/tcp
dc_hydra-setup-bank-a_1   bash -xc until curl -vsi " ...   Exit 0
dc_hydra-setup-bank-b_1   bash -xc until curl -vsi " ...   Exit 0
dc_hydra-setup-enki_1     bash -xc until curl -vsi " ...   Exit 0
dc_keyserver_1            /srv/entrypoint.sh               Up       8070/tcp
dc_postgres_1             docker-entrypoint.sh postgres    Up       0.0.0.0:5432->5432/tcp
dc_upspinserver_1         /srv/entrypoint.sh               Up       8090/tcp
```

To stop the running docker services, press `Ctrl+C` in the terminal where you started the services and the services should stop. Alternatively, you can do

```bash
make enki-down
```

The following services are now running:

| Name       | URL                    |
|------------|------------------------|
| Core       | http://localhost:3000  |

You can now go to the `bank-web-ui` folder and start `bank-a` by running:

```bash
NODE_ENV=dev BANK_THEME='bank-a' yarn start
```

## 2. Working on Core and Agent

### 2.1 Prerequisites on the host machine

Add bank-a's container dependency aliases to the hosts file to let enki and the agent find them.
Luckily they expose the same ports publicly what they expose to the other containers.
(Which is not true for bank-b...)

```bash
echo '127.0.0.1 hydra-bank-a keyserver' | sudo tee -a /etc/hosts
```

You will need an upspin client which understands the `-configonly` switch for `signup` commands.
You can compile it from the source included in the current repository:

```bash
cd <<repository root>>
mkdir -p ~/go/src
ln -s $(realpath upspin) ~/go/src/upspin.io
go build -o /usr/local/bin/upspin upspin.io/cmd/upspin
```

(This step requires go installed.)

### 2.2 Start the components

#### 2.2.1 Shared services and bank interfaces

Some containers will access resources on the host machine.
To let them do this we need to tell our machine's IP them through this environment variable:

```bash
export MY_EXTERNAL_IP=1.2.3.4
```

You can start bank-web-ui for Bank A and Bank B by running:

```bash
make bank
```

It can take a few minutes for the services to launch.

To make sure that the services are running as expected, in another terminal, run:

```bash
make bank-status
```

A sample correct output is:

```
         Name                        Command               State                         Ports
--------------------------------------------------------------------------------------------------------------------
dc_bank-a_1               /bin/sh -c ./node_modules/ ...   Up       0.0.0.0:9001->9001/tcp
dc_bank-b_1               /bin/sh -c ./node_modules/ ...   Up       0.0.0.0:9002->9002/tcp
dc_hydra-bank-a_1         sh -xc until /go/bin/hydra ...   Up       0.0.0.0:4444->4444/tcp
dc_hydra-bank-b_1         sh -xc until /go/bin/hydra ...   Up       0.0.0.0:4445->4444/tcp
dc_hydra-enki_1           sh -xc until /go/bin/hydra ...   Up       0.0.0.0:5444->4444/tcp
dc_hydra-setup-bank-a_1   bash -xc until curl -vsi " ...   Exit 0
dc_hydra-setup-bank-b_1   bash -xc until curl -vsi " ...   Exit 0
dc_hydra-setup-enki_1     bash -xc until curl -vsi " ...   Exit 0
dc_keyserver_1            /srv/entrypoint.sh               Up       0.0.0.0:8070->8070/tcp
dc_postgres_1             docker-entrypoint.sh postgres    Up       0.0.0.0:5432->5432/tcp
dc_upspinserver_1         /srv/entrypoint.sh               Up       0.0.0.0:8090->8090/tcp
```

To stop the running docker services, press `Ctrl+C` in the terminal where you started the services and the services should stop. Alternatively run, `make bank-down` to stop and delete the services.

The following services are now running:

| Name          | URL                       |
|---------------|---------------------------|
| Bank A        | http://localhost:9001     |
| Bank B        | http://localhost:9002     |

#### 2.2.2 Enki web interface

You can now go to the `core` folder and run:

```bash
yarn run webpack-watch
```

This is a blocking call.

#### 2.2.3 Enki API backend

In another terminal window in the `core` folder, run

```bash

export BASE_URI_BANK_A=http://localhost:9001
export BASE_URI_BANK_B=http://localhost:9002
export DATABASE_URL=jdbc:postgresql://localhost:5432/enki-core?user=postgres&password=postgres
export HYDRA_SERVER_URL=http://localhost:5444
export EXTERNAL_HYDRA_SERVER_URL=http://localhost:5444
export HTTP_PORT=3000
export HYDRA_ADMIN_LOGIN=admin
export HYDRA_ADMIN_PASSWORD=demo-password
export HYDRA_CLIENT_ID=consent-app
export HYDRA_CLIENT_SECRET=consent-secret
export OAUTH_BASE_URI_BANK_A=http://localhost:4444
export OAUTH_AUTHORIZE_URI_BANK_A=http://localhost:4444/oauth2/auth
export OAUTH_CLIENT_ID_BANK_A=enki-consumer
export OAUTH_CLIENT_SECRET_BANK_A=enki-secret
export OAUTH_BASE_URI_BANK_B=http://localhost:4445
export OAUTH_AUTHORIZE_URI_BANK_B=http://localhost:4445/oauth2/auth
export OAUTH_CLIENT_ID_BANK_B=enki-consumer
export OAUTH_CLIENT_SECRET_BANK_B=enki-secret
export OAUTH_BASE_URI_IRON_BANK=http://localhost:4446
export OAUTH_AUTHORIZE_URI_BANK_IRON_BANK=http://localhost:4446/oauth2/auth
export OAUTH_CLIENT_ID_IRON_BANK=enki-consumer
export OAUTH_CLIENT_SECRET_IRON_BANK=enki-secret
# From dc/enki.yaml's enki service's env variables.

export HOSTNAME=`hostname`

lein do clean, repl
user=> (repl/go)
```

#### 2.2.4 Set up upspin connection

Set up the agent's connection to upspin

```bash
dc/bank-local-dev/4.upspin-connect.sh
```

#### 2.2.5 Agent

In another terminal window in the `agent` folder, you should also start `agent` for Bank A

```bash
export ENKI_SERVER_URL=http://localhost:3000/
export BANK_NAME=bank-a
export OAUTH_CLIENT_ID=bank-a-client
export CONSUS_USER=test-agent1@test.labshift.io
export AGENT_URL=http://localhost:3010/
export UPSPINSERVER_FQDN=localhost
export UPSPINSERVER_PORT=8090
export CONSUS_CONFIG=${CONSUS_CONFIG:-$HOME/upspin}
# From dc/enki.yaml's agent-a service's env variables.

lein repl

user=> (require '[enki-agent.core :as core])
nil
user=> (core/-main "new-signing-key" "sign-a.key")
nil
user=> (core/-main "agent" "sign-a.key")
```

Note the last command is a blocking call.

### 2.3 Or all components in multiple screen sessions in one go

```bash
export MY_EXTERNAL_IP=1.2.3.4
make bank-screen
```

Then you can join to specific screen sessions with `screen -r` or
potentially you canreplace them with the above snippets
depending on which one you plan to work on.

## 3. Everything

You can start all the services by running:

```bash
make everything
```

To make sure that the services are running as expected, in another terminal, run:

```bash
make everything-status
```

A sample correct output is:

```
         Name                        Command                  State                    Ports
----------------------------------------------------------------------------------------------------------
dc_agent-a_1              bash -c /srv/wait-for-it - ...   Up (healthy)   0.0.0.0:3010->3010/tcp
dc_agent-b_1              bash -c /srv/wait-for-it - ...   Up (healthy)   3010/tcp, 0.0.0.0:3011->3011/tcp
dc_bank-a_1               /bin/sh -c yarn start            Up (healthy)   0.0.0.0:9001->9001/tcp
dc_bank-b_1               /bin/sh -c yarn start            Up (healthy)   0.0.0.0:9002->9002/tcp
dc_enki_1                 bash -c wait-for-it  --tim ...   Up (healthy)   0.0.0.0:3000->3000/tcp
dc_hydra-bank-a_1         sh -xc until /go/bin/hydra ...   Up (healthy)   0.0.0.0:4444->4444/tcp
dc_hydra-bank-b_1         sh -xc until /go/bin/hydra ...   Up (healthy)   0.0.0.0:4445->4444/tcp
dc_hydra-enki_1           sh -xc until /go/bin/hydra ...   Up (healthy)   0.0.0.0:5444->4444/tcp
dc_hydra-setup-bank-a_1   bash -xc /srv/configure-co ...   Exit 0
dc_hydra-setup-bank-b_1   bash -xc /srv/configure-co ...   Exit 0
dc_hydra-setup-enki_1     bash -xc /srv/configure-co ...   Exit 0
dc_keyserver_1            /srv/entrypoint.sh               Up (healthy)   8070/tcp
dc_postgres_1             docker-entrypoint.sh sh -c ...   Up (healthy)   0.0.0.0:5432->5432/tcp
dc_upspinserver_1         /srv/entrypoint.sh               Up (healthy)   8090/tcp
```

Note how everything other than the hydra-setup tasks are marked "Up (healthy)" and all the hydra-setup tasks have exited with code 0

To stop the running docker services, press `Ctrl+C` in the terminal where you started the services and the services should stop. Alternatively run, `make everything-down` to stop and delete the services.

Following services are now running:

| Name          | URL                     |
|---------------|-------------------------|
| Bank A        | http://localhost:9001   |
| Bank B        | http://localhost:9002   |
| Core          | http://localhost:3000   |

To run `ereshkigal` against the running services, go to `ereshkigal` subfolder and run:

```bash
make check ENKI_SERVER_URL=http://localhost:3000 BANK_URLS="http://localhost:9001 http://localhost:9002"
```

## Other

Docker images for core, agent and bank-web-ui are only built once. If you need to force refresh image creation, please run `make clean` before running any other command.

If you experience any yarn issues, please run the following in the necessary sub-directory:

```bash
rm -rf node_modules packages-cache
yarn cache clean
yarn install
```

## WSL Linux environments on Windows - additional notes

To interact correctly with Docker for Windows, add the following to your environment:

```bash
export DOCKER=docker.exe
export DOCKER_COMPOSE=docker-compose.exe
```

