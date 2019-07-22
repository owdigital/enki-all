FROM fedora:26
LABEL maintainer="Enki Team <enki-questions@oliverwyman.com>"

# Nosync means we can avoid fsyncs; this is safe, as docker layer building is atomic.
RUN dnf -y install nosync
RUN printf '#!/bin/sh\nexport LD_PRELOAD=/usr/lib64/nosync/nosync.so${LD_PRELOAD+:}${LD_PRELOAD}\nexec "$@"\n' > /usr/local/bin/nosync && chmod +x /usr/local/bin/nosync

# https://bugzilla.redhat.com/show_bug.cgi?id=1483553
RUN nosync dnf -y update libsolv
RUN nosync rpm --rebuilddb

ENV JENKINS_HOME /srv/jenkins
RUN groupadd -r jenkins -g 996
RUN useradd -c "Jenkins user" -d $JENKINS_HOME -g jenkins -m jenkins -u 1000
RUN echo "jenkins:jenkins" | chpasswd
RUN usermod -aG wheel jenkins

RUN groupadd -g 412 docker
RUN usermod -a -G docker jenkins

RUN nosync dnf -y update
RUN nosync dnf -y install java-1.8.0-openjdk dnf-plugins-core
RUN nosync dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
RUN nosync dnf -y install docker-ce

RUN curl --create-dirs -sSLo /usr/share/jenkins/slave.jar http://jenkins.gcp.labshift.io/jnlpJars/slave.jar \
  && chmod 755 /usr/share/jenkins \
  && chmod 644 /usr/share/jenkins/slave.jar

RUN curl -sSLo /usr/local/bin/jenkins-slave https://raw.githubusercontent.com/jenkinsci/docker-jnlp-slave/master/jenkins-slave && chmod +x /usr/local/bin/jenkins-slave

RUN curl -sSLo /usr/local/bin/wait-for-it https://raw.githubusercontent.com/vishnubob/wait-for-it/8ed92e8cab83cfed76ff012ed4a36cef74b28096/wait-for-it.sh && chmod +x /usr/local/bin/wait-for-it

RUN nosync dnf install -y git sqlite maven

ENV LEIN_ROOT 1
RUN curl -sSLo /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/2.7.1/bin/lein \
  && chmod 755 /usr/local/bin/lein

RUN lein self-install
RUN curl -L https://github.com/docker/compose/releases/download/1.16.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose

# Hashes from https://github.com/kubernetes/kubernetes/blob/923abd01493182873ace8c0410fb46a0c2ecd3b7/CHANGELOG-1.8.md#downloads-for-v184
RUN cd /tmp && curl -Lo kubernetes-client-linux-amd64.tar.gz https://dl.k8s.io/v1.8.4/kubernetes-client-linux-amd64.tar.gz && \
	{ echo 4d3c2a9e0d837e3607580d95bbc473ffb496fc47ba0ce7721e9180a9020f1f39 kubernetes-client-linux-amd64.tar.gz | sha256sum -c -; } && \
	tar zxvf kubernetes-client-linux-amd64.tar.gz kubernetes/client/bin/kubectl && \
	install -o root -g root -m 0755 kubernetes/client/bin/kubectl /usr/local/bin/kubectl

# unzip needed for chromedriver
RUN nosync dnf -y install unzip chromium
ENV CHROME_PATH /usr/bin/chromium-browser

# We're expecting chromedriver 63 which chromedriver 2.36 will support (see http://chromedriver.chromium.org/downloads)
ENV chromium-browser --version |grep 63.0

# Install chromedriver (for testing)
RUN mkdir -p /opt/google/chrome && \
    curl -L --silent http://chromedriver.storage.googleapis.com/2.36/chromedriver_linux64.zip -o /tmp/chromedriver_linux64.zip && \
    unzip -o -d /opt/google/chrome /tmp/chromedriver_linux64.zip && \
    chmod +x /opt/google/chrome/chromedriver && \
    rm /tmp/chromedriver_linux64.zip
ENV CHROME_DRIVER_PATH /opt/google/chrome/chromedriver

RUN mkdir /srv/jenkins/{.jenkins,.ssh,.docker,.kube}
COPY known_hosts /srv/jenkins/.ssh/
COPY docker.json /srv/jenkins/.docker/config.json
COPY kube.conf /srv/jenkins/.kube/config
RUN chown -R jenkins: /srv/jenkins

ENV LEIN_ROOT 1
RUN curl -sSLo /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/2.8.1/bin/lein \
  && chmod 755 /usr/local/bin/lein

RUN lein self-install

RUN nosync dnf install -y fedora-repos-rawhide && \
    nosync dnf install -y @development-tools @c-development python2 java-1.8.0-openjdk-devel && \
    nosync dnf install --enablerepo=rawhide -y --nogpgcheck nodejs && \
    nosync dnf install -y python3-devel python3-virtualenv golang python2 gettext

RUN npm install -g yarn

ENV GOPATH /go
ENV PATH $GOPATH/bin:$PATH
RUN mkdir -p "$GOPATH/src" "$GOPATH/bin" && chmod -R 777 "$GOPATH"

# Default Fedora policies block a lot of SSH keys, causing issues with builds
RUN rm /etc/crypto-policies/back-ends/openssh.config

USER jenkins
WORKDIR /srv/jenkins
RUN chmod 700 .ssh
RUN nosync dnf clean all
ENTRYPOINT ["jenkins-slave"]