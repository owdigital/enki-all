MODULE = docker

include ../build/common.mk

# Still want the plain "images" target for Jenkinsfile, but also be able to build jenkins slave manually
BUILD_IMGS = hydra-setup-bank hydra-setup-core postgres #jenkins-slave
IMGS = $(BUILD_IMGS) jenkins-slave
IMGES = $(patsubst %/Dockerfile,%,$(IMGS))
IMAGES = $(foreach i,$(IMGES),$(i).image)
PUSH_IMAGES = $(foreach i,$(IMGES),$(i).image.push)
IMAGE_STAMPS = $(foreach i,$(IMAGES),$(WORKDIR)/$(i).$(IMAGE_TAG).stamp)
PUSH_STAMPS = $(foreach i,$(PUSH_IMAGES),$(WORKDIR)/$(i).$(IMAGE_TAG).stamp)

all: images

print-%: ; @echo $* = $($*)

clean:
	rm -rf "$(WORKDIR)"

images: $(foreach i,$(BUILD_IMGS),$(i).image)
push-images: $(foreach i,$(BUILD_IMGS),$(i).image.push)

$(IMAGE_STAMPS): $(WORKDIR)/%.image.$(IMAGE_TAG).stamp: $(WORKDIR)/.exists %
	$(DOCKER) build -t enki/$* $*
	$(DOCKER) tag enki/$* enki/$*:$(IMAGE_TAG)
	touch $@

$(IMAGES): %.image: $(WORKDIR)/%.image.$(IMAGE_TAG).stamp

$(PUSH_STAMPS): $(WORKDIR)/%.image.push.$(IMAGE_TAG).stamp: $(WORKDIR)/.exists % $(WORKDIR)/%.image.$(IMAGE_TAG).stamp
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/$*:$(IMAGE_TAG) $(REGISTRY)enki/$*:$(IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/$*:$(IMAGE_TAG)
endif
	touch $@

$(PUSH_IMAGES): %.push: $(WORKDIR)/%.push.$(IMAGE_TAG).stamp

$(WORKDIR)/postgres.image.$(IMAGE_TAG).stamp: $(wildcard postgres/*)
$(WORKDIR)/hydra-setup-core.image.$(IMAGE_TAG).stamp: $(wildcard hydra-setup-core/*)
$(WORKDIR)/hydra-setup-bank.image.$(IMAGE_TAG).stamp: $(wildcard hydra-setup-bank/*)
$(WORKDIR)/jenkins-builder.image.$(IMAGE_TAG).stamp: $(WORKDIR)/jenkins-slave.image.$(IMAGE_TAG).stamp \
	$(wildcard jenkins-builder/*)
$(WORKDIR)/jenkins-slave.image.$(IMAGE_TAG).stamp: $(wildcard jenkins-slave/*)
