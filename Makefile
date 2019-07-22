HOSTNAME := $(shell hostname)
CONTAINER_UUID := $(shell uuidgen)
TOPDIR = $(realpath .)

include build/common.mk

.PHONY: enki enki-status enki-down bank bank-status everything \
  everything-status build-enki build-bank everything-down build-agent \
  clean build-agent-local build-enki-local build-bank-local prepare

all: everything

$(WORKDIR)/git-submodules:
	git submodule update --init
	touch $@

prepare: $(WORKDIR)/git-submodules

clean-%:
	$(MAKE) -C $* clean

clean: clean-core clean-docker clean-demo-suite clean-k8s clean-agent \
    clean-bank-web-ui clean-ereshkigal clean-upspin
	$(DOCKER) system prune --force

build-agent-local: build-common build-upspin
	$(MAKE) -C agent docker-image DOCKER_IMAGE=enki/agent AGENT_IMAGE_TAG=latest

build-enki-local: build-agent-local
	$(MAKE) -C core images CORE_IMAGE_TAG=latest

build-bank-local: build-agent-local
	$(MAKE) -C bank-web-ui docker-image BANK_WEB_IMAGE_TAG=latest

enki-down:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml down

enki: enki-down build-enki-local
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml up --build

enki-status:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml ps

bank-down:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/bank.yaml -f dc/bank-local-dev/overrides.yaml down

bank: bank-down build-bank-local
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/bank.yaml -f dc/bank-local-dev/overrides.yaml up --build

bank-bg: bank-down build-bank-local
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/bank.yaml -f dc/bank-local-dev/overrides.yaml up --build -d

bank-screen:
ifndef MY_EXTERNAL_IP
	$(error MY_EXTERNAL_IP is not set)
endif
	make bank-bg
	dc/bank-local-dev/1.webpack.watch.sh
	sleep 60
	dc/bank-local-dev/2.core.repl.sh
	dc/bank-local-dev/3.upspin-connect.sh
	sleep 30
	dc/bank-local-dev/4.agent.repl.sh

bank-screen-down:
	make bank-down
	dc/bank-local-dev/kill_screen_childs.sh

bank-status:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/bank.yaml -f dc/bank-local-dev/overrides.yaml ps

everything-down:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml down

everything: everything-down build-enki-local build-bank-local build-docker
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml up --build

everything-background: everything-down build-enki-local build-bank-local build-docker
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml up --build -d

everything-status:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml ps

everything-wait:
	./dc/wait-for-everything.sh

dc-test-down:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml -f dc/ereshkigal.yaml down

dc-test: dc-test-down build-enki-local build-bank-local build-docker build-ereshkigal
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml -f dc/ereshkigal.yaml up --build

dc-test-status:
	$(DOCKER_COMPOSE) -f dc/base.yaml -f dc/enki.yaml -f dc/bank.yaml -f dc/ereshkigal.yaml ps

build-common: prepare
	$(MAKE) -C common compile

build-agent: build-common build-upspin
	$(MAKE) -C agent images

build-core: build-common
	$(MAKE) -C core images

build-docker-jenkins:
	$(MAKE) -C docker jenkins

build-docker:
	$(MAKE) -C docker images

build-upspin: prepare
	$(MAKE) -C upspin images
	$(DOCKER) run --name ${CONTAINER_UUID} enki/upspin-client
	$(DOCKER) cp ${CONTAINER_UUID}:/usr/local/bin/upspin agent/docker/upspin
	$(DOCKER) rm -f ${CONTAINER_UUID}

build-%: prepare
	$(MAKE) -C $* images

images: build-common build-docker build-core build-agent \
    build-demo-suite build-upspin build-bank-web-ui build-ereshkigal

push-images-%: build-%
	$(MAKE) -C $* push-images

push-images: push-images-docker push-images-core push-images-agent \
    push-images-demo-suite push-images-upspin push-images-bank-web-ui \
    push-images-ereshkigal

deploy: push-images
	$(MAKE) -C k8s deploy-all
undeploy:
	$(MAKE) -C k8s undeploy-all
await: deploy
	$(MAKE) -C k8s await-all

test-%: images
	$(MAKE) -C $* test

test: dc-test

iron-bank-down:
	$(DOCKER_COMPOSE) -f dc/iron.yaml down

build-iron-bank:
	$(DOCKER) build -t enki/enki-iron-bank-web-ui:latest --build-arg BANK_THEME=iron-bank ./bank-web-ui/

iron-bank: build-iron-bank
	$(DOCKER_COMPOSE) -f dc/iron.yaml up

iron-bank-status:
	$(DOCKER_COMPOSE) -f dc/iron.yaml ps

deploy-check:
	make -C ereshkigal check ENKI_SERVER_URL=https://enki.${K8S_ZONE} CORE_HYDRA_URL=https://hydra.${K8S_ZONE}:4444 BANK_URLS="https://bank-a.${K8S_ZONE} https://bank-b.${K8S_ZONE}"

