TOPDIR ?= $(realpath ..)
WORKDIR = $(if $(MODULE),$(TOPDIR)/work/$(MODULE),$(TOPDIR)/work)
WORKDIR_EXISTS = $(WORKDIR)/.exists
VERSION_STAMP = $(WORKDIR)/.version

COMMON_VERSION = 0.20.1

AUTO_IMAGE_TAG = $(shell echo enki-dev-$$(git log -1 | grep Author | sed -e 's/[^<]*<\([^@]*\).*/\1/g' -e 's/+//g')-$$(date +%Y-%m-%d)-$$(git log -n 1 --pretty=format:'%h') )
IMAGE_TAG ?= $(AUTO_IMAGE_TAG)

AGENT_IMAGE_TAG ?= $(shell git ls-tree HEAD -- ../agent | awk '{ print $$3 }')
CORE_IMAGE_TAG ?= $(shell git ls-tree HEAD -- ../core | awk '{ print $$3 }')
BANK_WEB_IMAGE_TAG ?= $(shell git ls-tree HEAD -- ../bank-web-ui | awk '{ print $$3 }')
UPSPIN_IMAGE_TAG ?= $(shell find ../upspin -type f | sort | xargs md5sum | md5sum | awk '{ print $$1 }')

DOCKER ?= docker
DOCKER_COMPOSE ?= docker-compose

K8S_NAMESPACE ?= localdev
K8S_ZONE ?= enki.local
KUBECTL = kubectl
ifneq ($(K8S_CONTEXT),)
KUBECTL += --context $(K8S_CONTEXT)
endif
KUBECTL += -n $(K8S_NAMESPACE)

# REGISTRY = 286982628803.dkr.ecr.eu-central-1.amazonaws.com
REGISTRY = gcr.io/enki-198710/
BASE_IMAGE = fedora
REGISTRY_PROVIDER = $(if $(findstring amazonaws.com,$(REGISTRY)), \
	aws, \
	$(if $(findstring gcr.io/,$(REGISTRY)), \
	gke, \
	unknown \
))

$(WORKDIR_EXISTS):
	mkdir -vp $(WORKDIR)
	touch $@

VERSION_STAMP = $(WORKDIR)/.version

_ := $(shell $(TOPDIR)/build/ensure-git-version "$(IMAGE_TAG)" "$(VERSION_STAMP)" 1>&2)

$(VERSION_STAMP): $(WORKDIR_EXISTS)
	../build/ensure-git-version $(IMAGE_TAG) $(VERSION_STAMP)
