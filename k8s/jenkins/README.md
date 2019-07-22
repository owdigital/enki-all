# GCP Jenkins

This folder contains the kubernetes configuration definition yaml files for Jenkins as deployed on the GCP environment.

You'll need to setup a cluster-admin setup for your current user as per https://cloud.google.com/kubernetes-engine/docs/how-to/role-based-access-control#prerequisites_for_using_role-based_access_control
e.g. `kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user tom.parker-shemilt@oliverwyman.com`

## Setup notes
- Kubernetes URL: https://kubernetes.default
- Jenkins URL: http://jenkins-ui.jenkins.svc.cluster.local:8080
- Jenkins tunnel: jenkins-discovery.jenkins.svc.cluster.local:50000
- Builder pod container must be called jnlp (https://issues.jenkins-ci.org/browse/JENKINS-49511)
- Don't use workflow-cps/"Pipeline: Groovy" 2.59 (https://issues.jenkins-ci.org/browse/JENKINS-54186)
- Open advanced options under "Container template" and set Request CPU to 500m and Request Memory to 1024Mi (https://issues.jenkins-ci.org/browse/JENKINS-50132)
- Open advanced options under "Kubernetes pod template" and set Service Account to "jenkins"
- Install SSH Agent plugin and create a "SSH Username with private key" credential with id: `ssh` and username: `git` from with a user that has access to Bitbucket
- Make a Username and password credential with id `gcr-enki`, username: `_json_key` and password the json file from setting up a service account (see https://cloud.google.com/container-registry/docs/advanced-authentication#json_key_file)
  - Make sure the private key ends with a newline, or you'll get errors like "Error loading key "/srv/jenkins/workspace/enki_PR-86/core@tmp/private_key_5505786992090852356.key": invalid format"
- Make a secret file with id: `enki-kubectl-config` and contents as the results of `KUBECONFIG=jenkins-cd-kubeconf.conf gcloud container clusters get-credentials jenkins-cd --region europe-west3` (change as appropriate for current cluster)