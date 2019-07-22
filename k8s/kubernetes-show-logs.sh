#!/bin/bash
# Loop through all the containers except the hydra setup and
# display the last 100 lines of logs
set -e

# mandatory variables
: ${K8S_NAMESPACE?"K8S_NAMESPACE is mandatory"}

echo -e "Showing pod state:\n"
kubectl get po -n $K8S_NAMESPACE

pods=$(kubectl get po -n $K8S_NAMESPACE --no-headers -o name)
for pod in $pods
do
  containers=$(kubectl get -n $K8S_NAMESPACE $pod -o jsonpath='{.spec.containers[?(@.name != "hydra-setup")].name}')
  for container_name in $containers
  do
    echo -e "\nShowing logs for $pod, container $container_name:\n"
    kubectl logs -n $K8S_NAMESPACE $pod --tail=100 -c $container_name
  done
done
