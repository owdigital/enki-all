# Onboarding an Enki Partner

## Onboarding process
The process of onboarding a new partner to Enki consists of the following steps:
* Set up the Enki Agent
    * Run the Agent jar with the `new-signing-key` command, providing the name of the key as a command line option
* Register the Enki Core Hydra instance as a client of the partner's OAuth2 server 
    * Preferably the partner's OAuth2 server is ORY Hydra, as this simplifies the process
* Register the partner's OAuth2 server as a client of the Enki Core Hydra instance
    * Can be done by using `hydra connect` to connect to the Enki Core Hydra instance with an onboarding client credentials that has permissions to create new clients
    * Enki Core could in future have an internal onboarding portal that connects to the Hydra instance with the onboarding client credentials that would set up new client
* Run the Enki Agent
    * Run the Agent jar with the `agent` command, providing the name of the key to use as a command line option
    
The partner then needs to integrate their existing systems with the Agent and OAuth2 server. The time required for this integration will depend on the complexity of the systems in place at the partner.
    
## Registering the Enki Agent
Running the Enki Agent in the standard manner will setup and provision the Agent normally, i.e. running the following: 
```bash
export ENKI_SERVER_URL=...
lein run new-signing-key sign.key
lein run agent sign.key
```
As such the difficult part is setting up the OIDC client.

## Registering a new OIDC client with Enki
A new OIDC client can be registered with Enki by sending a post request to the `/clients` endpoint on the `hydra-enki` container. This post request should contain, at a minimum, the following:
1. `client_id` - string: Identifier to be used by the client
2. `redirect_uris` - array of strings: Allowed callback URIs for the client
3. `grant_types` - array of strings: Should be `["authorization_code","refresh_token","client_credentials","implicit"]`
4. `response_types` - array of strings: Should be `["token","code","id_token"]`
5. `allowed_scopes` - string containing a space-separated list of scope values: Should be `"openid offline hydra.clients hydra.keys.get user:uid"`

Additional fields can be found in the [Hydra API docs](https://www.ory.sh/docs/api/hydra/).

The new OIDC client then needs to register Enki as a client.

## Onboarding demo
A demo of onboarding a new bank can be performed from the root of the project as follows:
```bash
make everything
# Wait until Enki and Banks A and B are set up, then in another terminal:

make iron-bank
# This will setup & onboard a new bank, the Iron Bank, to Enki. 
```

The onboarding process will perform a post request to the central Enki Hydra instance to add the Iron Bank as an OIDC client of Enki, as well as having the Agent setting itself up with a new signing key, and registering the Agent and key with Enki Core and Upspin respectively. 
