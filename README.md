[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v1.4%20adopted-ff69b4.svg)](code-of-conduct.md)

# Enki

## Introduction

Enki is LShift's solution for secure messaging and data exchange, designed to comply with the [PSD2](https://en.wikipedia.org/wiki/Payment_Services_Directive) and [GDPR](https://eugdpr.org/) regulations. One of the key design principles of Enki was to remain rigorously agnostic as to where it’s run and to keep the onboarding ramp as shallow as possible while incorporating some of the most challenging (from a development and implementation perspective) security and authentication approaches ‘out of the box’.

## Licence

Copyright © 2016 - 2019 Oliver Wyman Ltd.

Enki is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Enki is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the [GNU General Public License](./LICENSE.md)
along with Enki.  If not, see [https://www.gnu.org/licenses/](https://www.gnu.org/licenses/).

## Acknowledgements

Enki makes use of a number of open-source packages. Please see our full list of [acknowledgements](./ACKNOWLEDGEMENTS.md).

## Contact us

If you have any questions or comments, please contact [enki-questions@oliverwyman.com](mailto:enki-questions@oliverwyman.com)

## Contributing to Enki

Before making a contribution, please look at our [guidelines for contributors](CONTRIBUTING.md).

Also, please make use of our Git pre-commit hook:

```bash
    cp .githooks/pre-commit .git/hooks
```

## Enki components

Enki is made up of a number of services that run as Docker images:

* [core](core/README.md) - The core enki service.
* [agent](agent/README.md) - An agent that allows a bank to communicate securely with enki.
* [bank-web-ui](bank-web-ui/README.md) - A sample bank web UI that demonstrates enki functionality.

In addition, the following utilities and tests are provided:

* [demo-suite](demo-suite/README.md) - The demo control suite for enki. 
* [ereshkigal](ereshkigal/README.md) - end to end smoke tests. 
* [upspin](upspin/README.md) - a preliminary model for the Consus store of PII data.

## Building and running enki docker images 

The top-level `makefile` has some convenience targets for developers. `make everything` should 
bring up a running system, as one example. See [docker.md](docker.md) for more information.

Do not forget to initialise the dependency submodules (Bletchley):
`git submodule update --init`

## Building for Deployment

You will need to have the following installed:

 * [`GNU make`](https://www.gnu.org/software/make/)
 * `envsubst` from [gettext](https://www.gnu.org/software/gettext/)
 * `timeout` from [GNU coreutils](https://www.gnu.org/software/coreutils/coreutils.html)
 * [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
 * [helm](https://docs.helm.sh/using_helm/)
 * `docker`
 * `docker-compose`
 * `npm`
 * [lein](https://leiningen.org/)
 * [yarn](https://yarnpkg.com/en/docs/install)

You should be able to install most of these through your package manager, eg: via dpkg or [Homebrew](https://brew.sh/).

The top-level `Makefile` has the following useful targets:

 * `images`: Builds docker images for the project.
    This uses `IMAGE_TAG`, or picks a default.
 * `push-images`: Depends on `images`, and pushes docker images to the registry specified as `REGISTRY`. This will default to [Google's GCR](https://console.cloud.google.com/gcr/).
 * `deploy`: Builds kubernetes configuration and applies it.
    Uses `K8S_CONTEXT` to determine which cluster to deploy to (cf: [Define clusters, users, and contexts](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/#define-clusters-users-and-contexts). `K8S_NAMESPACE` determines the [namespace](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/), and `K8S_ZONE` controls what domain name we configure the application under. Eg: if the demo script for staging lives at `demo.stage.enki.services`, then the zone is `stage.enki.services`.
 * `await` will then watch the state of resources in kubernetes, and check for them to be available via http/https. This acts as a trivial smoketest.

Each of these targets is cumulative, eg: `push-images` depends on `images`, and will rebuild anything that it sees has changed.

e.g: to deploy to AWS under a developer namespace:
```shell
make await \
  K8S_CONTEXT=enki.k8s.local \
  K8S_NAMESPACE=<name> \
  K8S_ZONE=enki-<name>.develop.enki.services
```

## Additional documentation

* [Terminology and User Journey] (core/docs/presentation.md)
* [Architecture](core/docs/architecture.md)
* [Core API](https://enki.production.enki.services/api-docs/index.html)
* [Agent API](agent/docs/api.md)
* [Onboarding an Enki Partner](partner_onboarding.md)
* [Adding dummy metadata and share assertions](core/docs/add_assertions.md)
* [Accessing PostgreSQL on k8s](core/docs/postgres-access.md)

