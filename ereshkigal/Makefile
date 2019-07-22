MODULE = ereshkigal

include ../build/common.mk

VENV?=$(WORKDIR)/venv

BASIC_USERNAME ?= enki
BASIC_PASSWORD ?=
ENKI_SERVER_URL ?= https://enki.stage.enki.services
HYDRA_URLS ?= https://hydra.stage.enki.services https://hydra-a.stage.enki.services https://hydra-b.stage.enki.services
BANK_URLS ?= https://bank-a.stage.enki.services https://bank-b.stage.enki.services

export BASIC_USERNAME BASIC_PASSWORD ENKI_SERVER_URL HYDRA_URLS BANK_URLS

TEST_FILES := $(shell find test -type f -name '*.py' -print )

.PHONY: all virtualenv freeze check fix setup setup-dev run test clean
all: $(VENV)/installed-stamp setup

clean:
	rm -rf .pytest_cache
	rm -rf $(WORKDIR)
	rm -rf ereshkigal.egg-info

check: $(VENV)/installed-stamp $(VENV)/check-flake8 test

fix: autopep8

virtualenv: $(VENV)/bin/python
$(VENV)/bin/python:
	python3 -m venv $(VENV)

$(VENV)/installed-stamp: $(VENV)/bin/python requirements.txt setup.py
	$(VENV)/bin/pip install -r requirements.txt
	$(VENV)/bin/pip install -e .
	touch $@

setup: $(VENV)/installed-stamp

freeze: $(VENV)/bin/python
	 $(VENV)/bin/pip freeze | grep -ve '^pkg-resources=' -e "lshift/ereshkigal" > new-requirements.txt
	@diff -u requirements.txt new-requirements.txt || true

$(VENV)/check-flake8: $(VENV)/installed-stamp $(TEST_FILES)
	$(VENV)/bin/flake8 $(TEST_FILES)

autopep8: $(VENV)/installed-stamp $(TEST_FILES)
	$(VENV)/bin/autopep8 --in-place --aggressive --aggressive --aggressive $(TEST_FILES)

test: $(VENV)/installed-stamp $(TEST_FILES)
	$(VENV)/bin/pytest --junitxml=$(WORKDIR)/junit-report.xml --capture=no -vvv -m "$(PYTEST_FILTER)"

compile: setup

$(WORKDIR)/image-ereshkigal-build.stamp: Dockerfile requirements-image.txt $(TEST_FILES)
	$(DOCKER) build -t enki/ereshkigal .
	$(DOCKER) tag enki/ereshkigal enki/ereshkigal:$(IMAGE_TAG)
	touch $@

$(WORKDIR)/image-ereshkigal-push.stamp: $(WORKDIR)/image-ereshkigal-build.stamp
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/ereshkigal:$(IMAGE_TAG) $(REGISTRY)enki/ereshkigal:$(IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/ereshkigal:$(IMAGE_TAG)
endif
	touch $@

images: $(WORKDIR)/image-ereshkigal-build.stamp

push-images: $(WORKDIR)/image-ereshkigal-push.stamp
