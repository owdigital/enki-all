## Setting up Consent app and Bank A web-ui for Hydra (assuming docker-compose workflow)

```
docker-compose exec hydra bash
```

Login into Hydra:

```bash
hydra connect --url http://localhost:4444 --id "admin" --secret "demo-password"
```

Create Consumer App

```bash
hydra clients create \
    --skip-tls-verify \
    --id bank-a-client \
    --name bank-a-client \
    --secret bank-a-secret \
    --grant-types authorization_code,refresh_token,client_credentials,implicit \
    --response-types token,code,id_token \
    --allowed-scopes openid,offline,hydra.clients,hydra.keys.get,firstName,lastName \
    --callbacks http://localhost:9001/signupcallback
```

Create Consent App:

```bash
hydra clients create \
  --skip-tls-verify \
  --id consent-app \
  --secret consent-secret \
  --name "ENKI Consent App Client" \
  --grant-types client_credentials \
  --response-types token \
  --allowed-scopes hydra.keys.get

hydra policies create \
  --skip-tls-verify \
  --actions get \
  --allow \
  --id consent-app-policy \
  --resources "rn:hydra:keys:hydra.consent.<.*>" \
  --subjects consent-app
```

Apply some global policies:

```bash
hydra policies create \
    --skip-tls-verify \
    --actions get \
    --description "Allow everyone to read the OpenID Connect ID Token public key" \
    --allow \
    --id openid-id_token-policy \
    --resources rn:hydra:keys:hydra.openid.id-token:public \
    --subjects "<.*>"


hydra policies create \
    --skip-tls-verify \
    --actions get \
    --description "Allow everyone to read the public keys" \
    --allow \
    --id public-key-policy \
    --resources "rn:hydra:keys:<[^:]+>:public" \
    --subjects "<.*>"
```
