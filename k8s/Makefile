MODULE = k8s

include ../build/common.mk

K8S_DEPS_CORE_SRC := core/ingress.yaml core/hydra.yaml core/pg-core.yaml
K8S_APP_CORE_SRC  :=  core/enki-core.yaml

DELETE_ARG ?=

K8S_NAMESPACE_STAMP = $(WORKDIR)/k8s-ns-$(K8S_NAMESPACE).stamp
ifneq ($(K8S_CONTEXT),)
KUBECTL_FLAGS = --context $(K8S_CONTEXT)
endif
export KUBECTL_FLAGS
export REGISTRY

all:

clean:
	rm -rf $(WORKDIR)

namespace: $(K8S_NAMESPACE_STAMP)
$(K8S_NAMESPACE_STAMP): $(WORKDIR_EXISTS)
	if ! $(KUBECTL) get namespace/$(K8S_NAMESPACE); then \
		$(KUBECTL) create namespace $(K8S_NAMESPACE); \
	fi
	#htpasswd -bc $(WORKDIR)/auth $(BASIC_USERNAME) $(BASIC_PASSWORD)                                    17s  feature/gcp-jenkins :: 22h :: ● :: ⬡
	#$(KUBECTL) -n $(K8S_NAMESPACE) create secret generic enki-basic-auth --from-file=$(WORKDIR)/auth
	touch $@

deploy-demo: $(WORKDIR_EXISTS) $(K8S_NAMESPACE_STAMP)
	demo-suite/deploy-branch -z "$(K8S_ZONE)" -f $(WORKDIR)/demo-suite.yaml $(K8S_NAMESPACE) $(IMAGE_TAG)
deploy-bank-web-ui: $(WORKDIR_EXISTS) $(K8S_NAMESPACE_STAMP)
	bank-web-ui/deploy-branch $(DELETE_ARG) -z "$(K8S_ZONE)" -f $(WORKDIR)/enki-bank-web-ui.yaml $(K8S_NAMESPACE) $(BANK_WEB_IMAGE_TAG) $(CORE_IMAGE_TAG) $(AGENT_IMAGE_TAG) $(UPSPIN_IMAGE_TAG) $(IMAGE_TAG) $(IMAGE_TAG)
await-demo: deploy-demo
	./demo-suite/show-config $(K8S_NAMESPACE)
	./wait-for-rollout $(K8S_NAMESPACE) deploy/demo-suite
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@demo.$(K8S_ZONE)/"
await-bank-web-ui: deploy-bank-web-ui
	./bank-web-ui/show-config $(K8S_NAMESPACE)
	./wait-for-rollout $(K8S_NAMESPACE) \
	sts/pg-bank-a sts/pg-bank-b deploy/bank-a-hydra deploy/bank-b-hydra \
	sts/consus-keyserver sts/consus-upspinserver \
	sts/bank-a-agent sts/bank-b-agent sts/bank-a-web-ui sts/bank-b-web-ui
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@bank-a.$(K8S_ZONE)/"
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@hydra-a.$(K8S_ZONE)/health"
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@bank-b.$(K8S_ZONE)/"
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@hydra-b.$(K8S_ZONE)/health"

# Core bits
$(WORKDIR)/core-dependencies.yaml: $(WORKDIR_EXISTS) $(VERSION_STAMP) core/make-k8s-config $(K8S_DEPS_CORE_SRC)
	core/make-k8s-config -f $@ -z "$(K8S_ZONE)" $(CORE_IMAGE_TAG) $(IMAGE_TAG) $(IMAGE_TAG) $(K8S_DEPS_CORE_SRC)

$(WORKDIR)/core-application.yaml: $(WORKDIR_EXISTS) $(VERSION_STAMP) core/make-k8s-config $(K8S_APP_CORE_SRC)
	core/make-k8s-config -f $@ -z "$(K8S_ZONE)" $(CORE_IMAGE_TAG) $(IMAGE_TAG) $(IMAGE_TAG) $(K8S_APP_CORE_SRC)

deploy-core-dependencies: $(WORKDIR)/core-dependencies.yaml \
		$(DEPLOY_PREREQS) \
		$(WORKDIR)/k8s-ns-$(K8S_NAMESPACE).stamp
	$(KUBECTL) apply -f $<

deploy-core-application: $(WORKDIR)/core-application.yaml deploy-core-dependencies
	$(KUBECTL) apply -f $<
	@echo Configured hostnames: $$($(KUBECTL) get ing/ingress  --output=jsonpath='{.spec.rules[*].host}{"\n"}')

configs: $(WORKDIR)/core-dependencies.yaml $(WORKDIR)/core-application.yaml

deploy-core: deploy-core-dependencies deploy-core-application
deploy-all: deploy-core deploy-demo deploy-bank-web-ui

undeploy-all:
	for i in [deployment sts svc]; do
		$(KUBECTL) delete $i --all
	done

await-core-dependencies: deploy-core-dependencies
	./wait-for-rollout $(K8S_NAMESPACE) sts/pg-core deploy/hydra
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@hydra.$(K8S_ZONE)/health"
await-core-application: deploy-core-application await-core-dependencies
	./wait-for-rollout $(K8S_NAMESPACE) deploy/enki-core
	./wait-until-http-ok.sh "https://$(BASIC_USERNAME):$(BASIC_PASSWORD)@enki.$(K8S_ZONE)/"

await-core: await-core-dependencies await-core-application
await-all: await-core await-demo await-bank-web-ui
