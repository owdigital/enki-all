MODULE = web-ui

include ../build/common.mk

SRC_FILES = $(shell find Dockerfile client server *.js \( -name db -prune \) -or -type f -print)

.PHONY: clean yarn-install docker-image

all: docker-image

clean:
	rm -rf $(WORKDIR)
	rm -rf $$HOME/.yarn2-mutex

total-destroy: clean
	rm -rf target node_modules packages-cache

yarn-install: $(WORKDIR)/yarn-install.stamp
	
$(WORKDIR)/yarn-install.stamp: $(WORKDIR_EXISTS) package.json yarn.lock
	yarn install --mutex=file:$$HOME/.yarn2-mutex --skip-integrity-check --force --ignore-scripts --prefer-offline
	touch $@

IMAGE_STAMPS = $(foreach img,bank-a bank-b,$(WORKDIR)/$(img).image.$(BANK_WEB_IMAGE_TAG).stamp)
PUSH_STAMPS = $(foreach img,bank-a bank-b,$(WORKDIR)/$(img).push.$(BANK_WEB_IMAGE_TAG).stamp)

$(IMAGE_STAMPS): $(WORKDIR)/%.image.$(BANK_WEB_IMAGE_TAG).stamp: $(SRC_FILES) $(WORKDIR)/yarn-install.stamp package.json yarn.lock webpack.config.js
	$(DOCKER) build -t enki/$*-web-ui:$(BANK_WEB_IMAGE_TAG) --build-arg BANK_THEME=$* .
	touch $@

$(PUSH_STAMPS): $(WORKDIR)/%.push.$(BANK_WEB_IMAGE_TAG).stamp: $(WORKDIR)/%.image.$(BANK_WEB_IMAGE_TAG).stamp
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/$*-web-ui:$(BANK_WEB_IMAGE_TAG) $(REGISTRY)enki/$*-web-ui:$(BANK_WEB_IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/$*-web-ui:$(BANK_WEB_IMAGE_TAG)
endif
	touch $@

bank-a-image bank-b-image: %-image: $(WORKDIR)/%.image.$(BANK_WEB_IMAGE_TAG).stamp
bank-a-push bank-b-push: %-push: $(WORKDIR)/%.push.$(BANK_WEB_IMAGE_TAG).stamp

images: docker-image
docker-image: $(IMAGE_STAMPS)
push-images: $(PUSH_STAMPS)

compile: yarn-install
