#!/usr/bin/env bash

set -uex

export CLUSTER_NAME=enki-prod
#export CONTEXT=gke_enki-prod_europe-west3_enki-prod
export PROJECT_NAME=enki-prod
export REGION=europe-west3

# cat the files together, but in a canonical (alpha) order, and with a separator.
awk 'FNR==1 && NR!=1 {print "---"}{print}' $(echo "$*"|tr " " "\n"|sort|tr "\n" " ") > all.yaml
patch < ingress.diff
patch < postgres.diff
patch < upspin-fix.diff
patch < fix-agent.diff

# Get the docker images across to gcr
aws ecr get-login --no-include-email --region eu-central-1 > /var/tmp/aws-login.sh
sh /var/tmp/aws-login.sh

for t in $(awk '/286982628803.dkr.ecr.eu-central-1.amazonaws.com/ { if (!seen[$0]++) print $2 }' all.yaml); do
  T=$(echo $t | sed 's/286982628803.dkr.ecr.eu-central-1.amazonaws.com//g');
  docker pull $t;
  docker tag $t eu.gcr.io/enki-prod$T;
  gcloud docker -- push eu.gcr.io/enki-prod$T;
done;

# fix the distribution for gke
sed -i .bak1 's/286982628803.dkr.ecr.eu-central-1.amazonaws.com/eu.gcr.io\/enki-prod/g' all.yaml
sed -i .bak2 -e ':a' -e 'N' -e '$!ba' -e 's/  annotations:\n    volume.beta.kubernetes.io\/storage-class: "[^"]*"\n//g' all.yaml
sed -i .bak3 -e "s/stage.enki.services/prodg.enki.services/" all.yaml
sed -i .bak4 -e 's/ReadWriteMany/ReadWriteOnce/g' all.yaml

# set up the cluster
export CLOUDSDK_CONTAINER_USE_V1_API_CLIENT=false
gcloud config set project $PROJECT_NAME
gcloud beta container clusters create $CLUSTER_NAME \
  --region $REGION --max-nodes=100\
  --machine-type=n1-standard-1 --enable-autoscaling \
  --enable-cloud-logging --enable-cloud-monitoring --image-type=ubuntu \
  --min-nodes=2  --tags=enkiproduction \
  --disk-size=100
 # --addons=HttpLoadBalancing,HorizontalPodAutoscaling,KubernetesDashboard
gcloud beta container clusters --region $REGION get-credentials $CLUSTER_NAME
echo Current Context $(kubectl config current-context)
kubectl create ns production
kubectl create -f tiller-rbac-config.yaml
helm init --service-account tiller
sleep 10 # until tiller starts
helm install --name enki-prod-ingress --values nginx-helm-values.yaml stable/nginx-ingress
htpasswd -bc auth enki Veyhyptucid5
kubectl -n production create secret generic enki-basic-auth --from-file=auth

kubectl -n production create -f all.yaml

# will watch for endpoint ip. stop when you get it.
kubectl --namespace default get services -o wide -w enki-prod-ingress-nginx-ingress-controller
# then go and update route53
# and when dns settles, trigger kube-lego with
helm install --name kube-lego-enki --set config.LEGO_EMAIL=james.uther@oliverwyman.com stable/kube-lego --set rbac.create=true --set config.LEGO_URL=https://acme-v01.api.letsencrypt.org/directory
# and if it doesn't work kick it with
kubectl delete pod $(kubectl get pods -l app=kube-lego | awk 'NR>1 {print $1}')
# and watch it complain about rate limits.


