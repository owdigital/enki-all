# enki-docker

A set of Dockerfiles that build Jenkins slaves prepared for the respective components of Enki.

## Creating a new Jenkins slave

1. Authenticate to Google Cloud. Full instructions are at https://cloud.google.com/container-registry/docs/advanced-authentication, but a simplified form is:
    a. `brew cask install google-cloud-sdk`
    b. `gcloud components install docker-credential-gcr`
    c. Follow caveats from `brew cask info google-cloud-sdk` to get your $PATH correctly set
    d. `docker-credential-gcr configure-docker`
    e. `docker-credential-gcr gcr-login`
2. Make and push the new image with `make jenkins-slave.image.push`
3. Goto the [Google Cloud Console](https://console.cloud.google.com/) and look at "Container Registry" > "Images" to find your new image
4. Configure Jenkins to use that new image