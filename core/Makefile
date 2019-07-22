MODULE = core

include ../build/common.mk

K8S_DEPS_SRC := k8s/ingress.yaml k8s/hydra.yaml k8s/pg-core.yaml
K8S_APP_SRC  := k8s/enki-core.yaml

all: deploy

KUBECTL = kubectl --context $(K8S_CONTEXT) -n $(K8S_NAMESPACE)

.PHONY: start clean total-destroy deploy deploy-dependencies \
	deploy-application purge images push-images \
	yarn-install enki-jar test

clean:
	rm -rf $(WORKDIR)
	rm -rf $$HOME/.yarn2-mutex
	rm -rf target

total-destroy: clean
	rm -rf target node_modules libs

# We probably only want to get rid of things that relate to the application.
purge:
	$(KUBECTL) delete all --all

cluster-status:
	minikube status

$(WORKDIR)/minikube-storage.stamp: k8s/manual/minikube-storage.yaml
	kubectl --context minikube apply -f $<
	touch $@

yarn-install: $(WORKDIR)/yarn-install.stamp

$(WORKDIR)/yarn-install.stamp: $(WORKDIR_EXISTS) package.json yarn.lock
	yarn install --frozen-lockfile --non-interactive --mutex=file:$$HOME/.yarn2-mutex --skip-integrity-check
	touch $@

lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar:
	lein deploy local enki/common $(COMMON_VERSION) ../common/target/common-$(COMMON_VERSION).jar

enki-jar: target/enki.jar
target/enki.jar: $(WORKDIR_EXISTS) $(shell find src resources -type f) webpack.config.js $(WORKDIR)/yarn-install.stamp lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar
	lein do with-profile dev deps
	NODE_ENV=production ../build/lein build-with-webpack

$(WORKDIR)/image-core-build.stamp: $(VERSION_STAMP) lib/enki/common/$(COMMON_VERSION)/common-$(COMMON_VERSION).jar webpack.config.js package.json yarn.lock $(WORKDIR)/yarn-install.stamp $(shell find {src,lib,resources} -type f)
	$(DOCKER) build -t enki/core .
	$(DOCKER) tag enki/core enki/core:$(CORE_IMAGE_TAG)
	touch $@

$(WORKDIR)/image-core-push.stamp: $(WORKDIR)/image-core-build.stamp
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/core:$(CORE_IMAGE_TAG) $(REGISTRY)enki/core:$(CORE_IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/core:$(CORE_IMAGE_TAG)
endif
	touch $@

$(WORKDIR)/image-core-test.stamp: images $(WORKDIR)/yarn-install.stamp
	npm run unitTest
	mkdir -p target
	rm -f .ssh upspin sign.key
	$(DOCKER_COMPOSE) --project-name $(CORE_IMAGE_TAG)_core -f docker-compose-test.yml rm -v --force --stop
	$(DOCKER_COMPOSE) --project-name $(CORE_IMAGE_TAG)_core -f docker-compose-test.yml up --build --abort-on-container-exit
	#docker cp `docker-compose --project-name $(CORE_IMAGE_TAG)_core -f docker-compose-test.yml ps -q core-test`:/app/target/test-reports/ target/ || true
	touch $@

images: $(WORKDIR)/image-core-build.stamp
push-images: $(WORKDIR)/image-core-push.stamp
test: $(WORKDIR)/image-core-test.stamp


compile: enki-jar
