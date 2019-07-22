MODULE = demo-suite

include ../build/common.mk

VENV?=$(WORKDIR)/venv

PY_FILES := $(shell find app.py -type f -name '*.py' -print )

.PHONY: all virtualenv freeze check fix setup setup-dev run clean
all: $(VENV)/installed-stamp setup

clean:
	rm -rf $(WORKDIR)
	find . -regex "\(.*__pycache__.*\|*.py[co]\)" -delete

check: $(VENV)/installed-stamp $(VENV)/check-flake8

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
	 $(VENV)/bin/pip freeze | grep -ve '^pkg-resources=' > new-requirements.txt
	@diff -u requirements.txt new-requirements.txt || true

$(VENV)/check-flake8: $(VENV)/installed-stamp $(PY_FILES)
	$(VENV)/bin/flake8 $(PY_FILES)

autopep8: $(VENV)/installed-stamp $(PY_FILES)
	$(VENV)/bin/autopep8 --in-place --aggressive --aggressive --aggressive $(PY_FILES)

run: $(VENV)/installed-stamp
	$(VENV)/bin/python app.py

$(WORKDIR)/image-demo-suite-build.$(IMAGE_TAG).stamp: $(VERSION_STAMP) $(PY_FILES) requirements.txt
	$(DOCKER) build -t enki/demo-suite .
	$(DOCKER) tag enki/demo-suite enki/demo-suite:$(IMAGE_TAG)
	touch $@

$(WORKDIR)/image-demo-suite-push.$(IMAGE_TAG).stamp: $(WORKDIR)/image-demo-suite-build.$(IMAGE_TAG).stamp
ifneq ($(strip $(REGISTRY)),)
	$(DOCKER) tag enki/demo-suite:$(IMAGE_TAG) $(REGISTRY)enki/demo-suite:$(IMAGE_TAG)
	$(DOCKER) push $(REGISTRY)enki/demo-suite:$(IMAGE_TAG)
endif
	touch $@

images: $(WORKDIR)/image-demo-suite-build.$(IMAGE_TAG).stamp
push-images: $(WORKDIR)/image-demo-suite-push.$(IMAGE_TAG).stamp
