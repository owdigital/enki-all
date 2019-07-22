# enki-demo-suite

## Overview

This project provides a demonstration of using Enki

## Prerequisites

The Enki `core`, `agent` and `bank-ui` docker images should all be running. This can be achieved by
executing `make everything` in a separate shell.

For the links in the demo to work, you will need to set an environment variable `ZONE` (if not, the
value `None` will be used). The demo will then expect to find the services at the following locations:

* Enki: https://enki.{zone}
* Bank A: https://bank-a.{zone}
* Bank B: https://bank-b.{zone}

## Build and run

Execute

```sh
make run
``` 

and open [`http://localhost:8080`](`http://localhost:8080`).

