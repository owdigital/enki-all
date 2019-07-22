Enki on GCP
===========

Ingress (once per cluster)
--------------------------
1. `kubectl create -f gcp/tiller-rbac-config.yaml`
2. `helm init --service-account tiller`
3. Wait until tiller starts...
4. `helm install --name enki-dev-ingress --values gcp/nginx-helm-values.yaml stable/nginx-ingress`
5. `htpasswd -bc auth enki PASSWORD`
6. `kubectl -n production create secret generic enki-basic-auth --from-file=auth`
7. Wait until you see `EXTERNAL-IP`, and note that to add to the DNS later
8. `kubectl --namespace default get services -o wide -w enki-prod-ingress-nginx-ingress-controller`
9. Make a Cloud DNS entry for *.temp-enki.gcp.labshift.io, mapping from Route53

SSL (per user)
---------------
Make service account with DNS administrator and save key to enki-dns.json
```
export NAME=james-uther
gcloud iam service-accounts create ${SOMEONE}-dns-account --display-name "James DNS account"
gcloud projects add-iam-policy-binding enki-198710 \
    --member serviceAccount:${SOMEONE}-dns-account@enki-198710.iam.gserviceaccount.com --role roles/dns.admin
gcloud iam service-accounts keys create sa-${SOMEONE}-dns-key.json \
  --iam-account ${SOMEONE}-dns-account@enki-198710.iam.gserviceaccount.com

export NS=enki-dev-${SOMEONE}
export ZONE=$NS.temp-enki.gcp.labshift.io
export EMAIL=${SOMEONE}@oliverwyman.com
export GCE_PROJECT=enki-198710
export GCE_SERVICE_ACCOUNT_FILE=sa-${SOMEONE}-dns-key.json
lego --path=$HOME/.lego --email=$EMAIL $(for s in enki hydra demo {bank,hydra}-{a,b}; do echo -d $s.$ZONE; done) --dns=gcloud run
 
for secret in enki-tls bank-tls; do
  kubectl -n $NS create secret tls $secret --cert $HOME/.lego/certificates/enki.$ZONE.crt --key $HOME/.lego/certificates/enki.$ZONE.key
done
```