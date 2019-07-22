#!/usr/bin/env bash
 
set -uex
 
export CLUSTER_NAME=prodg
export PROJECT_NAME=enki-198710
export NS=production
export ZONE=prodg.enki.services
export EMAIL=james.uther@oliverwyman.com

 
# cat the files together, but in a canonical (alpha) order, and with a separator.
awk 'FNR==1 && NR!=1 {print "---"}{print}' $(echo "$*"|tr " " "\n"|sort|tr "\n" " ") > all.yaml
patch < ingress.diff
 
# # Get the docker images across to gcr
# aws ecr get-login --no-include-email --region eu-central-1 > /var/tmp/aws-login.sh
# sh /var/tmp/aws-login.sh
# gcloud config set project ${PROJECT_NAME}
# gcloud auth configure-docker

# for t in $(awk '/286982628803.dkr.ecr.eu-central-1.amazonaws.com/ { if (!seen[$0]++) print $2 }' all.yaml); do
#  T=$(echo $t | sed 's/286982628803.dkr.ecr.eu-central-1.amazonaws.com//g');
#  docker pull $t;
#  docker tag $t eu.gcr.io/${PROJECT_NAME}$T;
#  docker push eu.gcr.io/${PROJECT_NAME}$T;
# done;
 
# fix the distribution for gke
# sed -i .bak1 's/286982628803.dkr.ecr.eu-central-1.amazonaws.com/eu.gcr.io\/${PROJECT_NAME}/g' all.yaml
# sed -i .bak2 -e ':a' -e 'N' -e '$!ba' -e 's/ annotations:\n volume.beta.kubernetes.io\/storage-class: "aws-efs"\n//g' all.yaml
sed -i .bak3 -e "s/stage.enki.services/${ZONE}/" all.yaml
sed -i .bak3 -e "s/hydra-tls/enki-tls/" all.yaml

# set up the cluster - you don't always want to create the cluster. Edit as appropriate.
gcloud config set container/use_v1_api false
gcloud config set project $PROJECT_NAME
gcloud beta container clusters create $CLUSTER_NAME \
 --zone europe-west3-a --max-nodes=100\
 --machine-type=g1-small --enable-autorepair --enable-autoscale --enable-autoupgrade \
 --enable-cloud-logging --enable-cloud-monitoring --image-type=cos \
 --min-nodes=1 --tags=enkidev \
 --disk-size=100 \
 --scopes https://www.googleapis.com/auth/trace.append


gcloud beta container clusters get-credentials $CLUSTER_NAME
export CONTEXT=$(kubectl config current-context)
echo Current Context $CONTEXT
kubectl create ns $NS
kubectl create -f tiller-rbac-config.yaml
helm init --service-account tiller

helm install --name nginx-ingress --values nginx-helm-values.yaml stable/nginx-ingress

# cat prod-enki-tls.yaml| yq '.data|."tls.crt"' | sed 's/"//g' | base64 --decode > enki.crt
# openssl x509 -in enki.crt -text -noout
kubectl create -n $NS -f ${CLUSTER_NAME}-enki-tls.yaml
#kubectl create -n $NS -f ${CLUSTER_NAME}-hydra-tls.yaml # prodg has hydra and other certs spit.
#helm install --name cert-manager --namespace kube-system stable/cert-manager
#kubectl create -f prod-cluster-issuer.yaml
#kubectl describe clusterissuer letsencrypt-production

#helm upgrade cert-manager stable/cert-manager --namespace kube-system \
#    --set ingressShim.extraArgs='{--default-issuer-name=letsencrypt-production,--default-issuer-kind=ClusterIssuer}'
#kubectl get certificates --all-namespaces
#kubectl describe certificate enki-tls
htpasswd -bc auth enki Veyhyptucid5
# actually, acme can't validate the service if we have auth on it, so first need to get tls working, then add in the basic auth
kubectl -n $NS create secret generic enki-basic-auth --from-file=auth

#lego --path=$HOME/.lego --email=$EMAIL $(for s in enki hydra demo {bank,hydra}-{a,b}; do echo -d $s.$ZONE; done) --dns=route53 run

#for secret in enki-tls hydra-tls; do
# kubectl --context=${CONTEXT} -n $NS create secret tls $secret --cert $HOME/.lego/certificates/enki.$ZONE.crt --key $HOME/.lego/certificates/enki.$ZONE.key
#done
 
kubectl create -n $NS -f all.yaml

#cd ereshkigal && make all check BASIC_USERNAME=... BASIC_PASSWORD=... ENKI_SERVER_URL=https://enki.prodg.enki.services CORE_HYDRA_URL=http://hydra.prodg.enki.services BANK_URLS="https://bank-a.prodg.enki.services https://bank-b.prodg.enki.services"

#gcloud container builds log --stream $(gcloud container builds list --limit 1 | awk 'NR > 1 {print $1}')