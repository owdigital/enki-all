MODULE = agent

include ../build/common.mk

SHELL=/bin/bash
UBERJAR = target/uberjar/enki-agent.jar

DOCKER_WORKDIR = $(WORKDIR)/docker
DOCKER_WORKDIR_EXISTS = $(DOCKER_WORKDIR)/.exists
DOCKER_IMAGE_DEPS = $(DOCKER_WORKDIR_EXISTS) \
	$(DOCKER_WORKDIR)/Dockerfile \
	$(DOCKER_WORKDIR)/$(notdir $(UBERJAR)) \
	$(DOCKER_WORKDIR)/run.sh \
	$(DOCKER_WORKDIR)/wait-for-it.sh \
	$(DOCKER_WORKDIR)/configure-upspin.sh

DOCKER_IMAGE = enki/agent

.PHONY: all uberjar clean

all: docker-image

uberjar: $(UBERJAR)

compile: uberjar

lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar:
	../build/lein deploy local enki/common $(COMMON_VERSION) ../common/target/common-$(COMMON_VERSION).jar

$(UBERJAR): project.clj $(shell find src -type f) lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar
	lein uberjar

$(DOCKER_WORKDIR_EXISTS):
	mkdir -vp $@
	touch $@

$(DOCKER_WORKDIR)/Dockerfile: docker/Dockerfile $(DOCKER_WORKDIR_EXISTS)
	cp $< $@
$(DOCKER_WORKDIR)/run.sh: docker/run.sh $(DOCKER_WORKDIR_EXISTS)
	cp $< $@
$(DOCKER_WORKDIR)/wait-for-it.sh: test/wait-for-it.sh $(DOCKER_WORKDIR_EXISTS)
	cp $< $@
$(DOCKER_WORKDIR)/configure-upspin.sh: test/configure-upspin.sh $(DOCKER_WORKDIR_EXISTS)
	cp $< $@
$(DOCKER_WORKDIR)/$(notdir $(UBERJAR)): $(UBERJAR) $(DOCKER_WORKDIR_EXISTS)
	cp $< $@

images: docker-image
docker-image: $(WORKDIR)/docker-image.$(AGENT_IMAGE_TAG).stamp

$(WORKDIR)/docker-image.$(AGENT_IMAGE_TAG).stamp: $(shell find src -type f) Dockerfile docker/run.sh test/wait-for-it.sh test/configure-upspin.sh lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar
	$(DOCKER) build -t $(DOCKER_IMAGE) .
	$(DOCKER) tag enki/agent enki/agent:$(AGENT_IMAGE_TAG)
	touch $@

clean:
	rm -rf docker-image.stamp $(WORKDIR) $(UBERJAR)

$(WORKDIR)/image-agent-push.$(AGENT_IMAGE_TAG).stamp: $(WORKDIR)/docker-image.$(AGENT_IMAGE_TAG).stamp $(PUSH_PREREQS)
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/agent:$(AGENT_IMAGE_TAG) $(REGISTRY)enki/agent:$(AGENT_IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/agent:$(AGENT_IMAGE_TAG)
endif
	touch $@

test: images
	lein test
	rm -f .ssh upspin sign.key
	$(DOCKER_COMPOSE) --project-name $(AGENT_IMAGE_TAG)_agent -f test/docker-compose-test.yml --project-directory . rm -v --stop --force
	$(DOCKER_COMPOSE) --project-name $(AGENT_IMAGE_TAG)_agent -f test/docker-compose-test.yml --project-directory . up --build --abort-on-container-exit
	$(DOCKER) cp `docker-compose --project-name $(AGENT_IMAGE_TAG)_agent -f test/docker-compose-test.yml --project-directory . ps -q agent-test`:/tests/integration.xml target/test-reports/
	$(DOCKER_COMPOSE) --project-name $(AGENT_IMAGE_TAG)_agent -f test/docker-compose-test.yml --project-directory . rm --stop --force

push-images: $(WORKDIR)/image-agent-push.$(AGENT_IMAGE_TAG).stamp
