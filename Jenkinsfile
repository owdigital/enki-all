def registry = 'https://gcr.io/enki-198710/'
def creds = 'gcr-enki'

def images = [
  'hydra-setup-core',
  'hydra-setup-bank',
  'postgres'
]

def pull_existing_image(CHECK_ENV, image, tag, stamp) {
  if (CHECK_ENV == "0") {
    sh "docker pull gcr.io/enki-198710/enki/${image}:${tag}"
    sh "docker tag gcr.io/enki-198710/enki/${image}:${tag} enki/${image}:latest"
    def stamp_path = "work/${stamp}"
    def stamp_dir = sh(script: "dirname ${stamp_path}", returnStdout: true).trim()
    sh "mkdir -p ${stamp_dir}"
    if (!fileExists("${stamp_dir}/.exists")) {
      touch "${stamp_dir}/.exists"
    }
    if (!fileExists("${stamp_dir}/yarn-install.stamp")) {
      touch "${stamp_dir}/yarn-install.stamp" // not needed by everyone, but generally useful
    }
    // Note that this feels like it should be tag, not env.IMAGE_TAG but our Makefiles aren't that smart...
    sh "./build/ensure-git-version ${env.IMAGE_TAG} ${stamp_dir}/.version"
    touch stamp_path
  }
}

pipeline {
  agent {
    kubernetes {
      label 'enki-builder'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
labels:
  component: ci
spec:
  # Use service account that can deploy to all namespaces
  serviceAccountName: jenkins
  securityContext:
    fsGroup: 412 # Group ID of docker group on k8s nodes
  containers:
  - name: jnlp # to avoid https://issues.jenkins-ci.org/browse/JENKINS-49511
    image: gcr.io/enki-198710/enki/jenkins-slave:2019-03-05
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
    }
  }

  stages {
    stage('Setup') {
      steps {
        script {
          git = fileLoader.load("build/git-tools.groovy");
          sh 'make prepare'
          escaped_build_tag = git.build_tag();
          env.IMAGE_TAG = escaped_build_tag
          author = git.build_author();
          namespace = git.determine_k8s_namespace(author)
          dns_zone = env.BRANCH_NAME == "develop" ? "stage.enki.services" : "${namespace}.develop.enki.services";
          docker_compose_project = BUILD_TAG.replace("%2F", ".").replace("-", "_")

          env.K8S_NAMESPACE = namespace
          env.K8S_ZONE = dns_zone

          // To get make to explain why it is doing what.
          env.MAKEFLAGS = '--trace'

          sh 'curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-238.0.0-linux-x86_64.tar.gz'
          sh 'tar -zxf google-cloud-sdk-238.0.0-linux-x86_64.tar.gz'
          withCredentials([usernamePassword(credentialsId: creds, usernameVariable: 'USER', passwordVariable: 'GCLOUD_JSON')]) {
            writeFile(file: 'gcloud.json', text: env.GCLOUD_JSON)
            def auth = readJSON file: 'gcloud.json'
            sh "./google-cloud-sdk/bin/gcloud auth activate-service-account ${auth['client_email']} --key-file=gcloud.json"
          }
          env.ACCESS_TOKEN = sh(script: "./google-cloud-sdk/bin/gcloud auth print-access-token", returnStdout: true).trim()
        }
      }
    }

    stage('Debug') {
      steps {
        sh 'docker version'
        echo "Using docker image tag: ‘${escaped_build_tag}’"
        echo "Using author ‘${author}’"
        echo ((namespace == null) ? "No k8s namespace; not deploying" : "Using namespace ‘${namespace}’")
        echo "Using dns_zone ‘${dns_zone}’"
        echo "Using docker-compose project prefix ‘${docker_compose_project}’"
      }
    }

    stage('Checks') {
      parallel {
        stage('Agent') {
          steps {
            script {
              git = fileLoader.load("build/git-tools.groovy");
              env.AGENT_IMAGE_TAG = "${git.hash_folder("agent")}-${git.hash_folder("common")}"
              env.HAVE_AGENT_IMAGE = git.has_image("agent", env.AGENT_IMAGE_TAG);
            }
          }
        }

        stage('Bank-Web-UI') {
          steps {
            script {
              git = fileLoader.load("build/git-tools.groovy");
              env.BANK_WEB_IMAGE_TAG = git.hash_folder("bank-web-ui")
              env.HAVE_BANK_A_IMAGE = git.has_image("bank-a-web-ui", env.BANK_WEB_IMAGE_TAG);
              env.HAVE_BANK_B_IMAGE = git.has_image("bank-b-web-ui", env.BANK_WEB_IMAGE_TAG);
            }
          }
        }

        stage('Core') {
          steps {
            script {
              git = fileLoader.load("build/git-tools.groovy");
              env.CORE_IMAGE_TAG = "${git.hash_folder("core")}-${git.hash_folder("common")}"
              env.HAVE_CORE_IMAGE = git.has_image("core", env.CORE_IMAGE_TAG);
            }
          }
        }

        stage('Upspin') {
          steps {
            script {
              git = fileLoader.load("build/git-tools.groovy");
              env.UPSPIN_IMAGE_TAG = sh(
                   script: 'find upspin -type f | sort | xargs md5sum | md5sum | awk \'{ print $1}\'',
                   returnStdout: true).trim()
              // env.UPSPIN_IMAGE_TAG = git.hash_folder("upspin")
              env.HAVE_UPSPIN_CLIENT_IMAGE = git.has_image("upspin-client", env.UPSPIN_IMAGE_TAG);
              env.HAVE_UPSPIN_KEYSERVER_IMAGE = git.has_image("upspin-keyserver", env.UPSPIN_IMAGE_TAG);
              env.HAVE_UPSPIN_SERVER_IMAGE = git.has_image("upspin-server", env.UPSPIN_IMAGE_TAG);
            }
          }
        }
      }
    }

    stage('Clean') {
      steps {
        dir("core") {
          sh 'make clean'
        }
        dir("bank-web-ui") {
          sh 'make clean'
        }
      }
    }

    stage('Prepare') {
      parallel {
        stage('Prepare-Docker') {
          steps {
            dir("docker") {
              sh 'make postgres.image'
            }
          }
        }

        // We do this one here so we can fish out the upspin image for the Build-Agent step
        stage('Build-Upspin') {
          when {
            expression {
              return HAVE_AGENT_IMAGE != "0" || HAVE_UPSPIN_CLIENT_IMAGE != "0" || HAVE_UPSPIN_SERVER_IMAGE != "0" || HAVE_UPSPIN_KEYSERVER_IMAGE != "0"
            }
          }
          steps {
            script {
              sh 'make build-upspin'
            }
          }
        }

        stage('Prepare-Common') {
          steps {
            dir("common") {
              sh 'make compile'
            }
            stash includes: 'common/target/common-*.jar', name: 'common-jar'
          }
        }

        stage('Prepare-Core') {
          when {
            expression {
              return HAVE_CORE_IMAGE != "0"
            }
          }
          steps {
            dir("core") {
              sshagent(['ssh']) {
                sh '../build/retry.sh 5 make yarn-install'
              }
            }
          }
        }

        stage('Prepare-Web-UI') {
          when {
            expression {
              return HAVE_BANK_A_IMAGE != "0" || HAVE_BANK_B_IMAGE != "0"
            }
          }
          steps {
            dir("bank-web-ui") {
              sshagent(['ssh']) {
                sh '../build/retry.sh 5 make yarn-install'
                sh 'yarn add sqlite3'
              }
            }
          }
        }

      }
    }

    stage('Build') {
      parallel {
        stage('Build-Docker') {
          steps {
            script {
              sh "make -C docker images"
            }
          }
        }

        stage('Build-Core') {
          when {
            expression {
              return HAVE_CORE_IMAGE != "0"
            }
          }
          steps {
            unstash 'common-jar'
            sh "make build-core"
          }
        }

        stage('Build-Agent') {
          when {
            expression {
              return HAVE_AGENT_IMAGE != "0"
            }
          }
          steps {
            unstash 'common-jar'
            dir("agent") {
              script {
                sh 'make uberjar'
                sh "make docker-image"
              }
            }
          }
        }

        stage('Build-Web-UI Bank A') {
          when {
            expression {
              return HAVE_BANK_A_IMAGE != "0"
            }
          }
          steps {
            script {
              sh "./build/retry.sh 5 make -C bank-web-ui bank-a-image"
            }
          }
        }

        stage('Build-Web-UI Bank B') {
          when {
            expression {
              return HAVE_BANK_B_IMAGE != "0"
            }
          }
          steps {
            script {
              sh "./build/retry.sh 5 make -C bank-web-ui bank-b-image"
            }
          }
        }
        stage("Build Demo") {
          steps {
            script {
              sh "make -C demo-suite images"
            }
          }
        }
      }
    }

    stage('Test') {
      parallel {
        stage('Test-Upspin') {
          when {
            expression {
              return HAVE_UPSPIN_CLIENT_IMAGE != "0" || HAVE_UPSPIN_SERVER_IMAGE != "0" || HAVE_UPSPIN_KEYSERVER_IMAGE != "0"
            }
          }
          steps {
            script {
              dir("upspin") {
                sh "docker-compose --project-name ${docker_compose_project}_upspin rm --stop -f -v"
                sh "docker-compose --project-name ${docker_compose_project}_upspin up --build --abort-on-container-exit"
                sh "docker-compose --project-name ${docker_compose_project}_upspin rm --stop -f -v"
              }
            }
          }
        }

        stage('Test-Common') {
          when {
            expression {
              return HAVE_CORE_IMAGE != "0" || HAVE_AGENT_IMAGE != "0" // because these two use common
            }
          }
          steps {
            dir("common") {
              sh 'lein test'
            }
          }
        }

        stage('Test-Core') {
          when {
            expression {
              return HAVE_CORE_IMAGE != "0"
            }
          }
          steps {
            dir("core") {
              script {
                sh 'yarn unitTest'
                sh 'rm -f .ssh upspin sign.key'
                sh "docker-compose --project-name ${docker_compose_project}_core -f docker-compose-test.yml rm -v --force --stop"
                sh "docker-compose --project-name ${docker_compose_project}_core -f docker-compose-test.yml up --build --abort-on-container-exit"
                def containerName = sh(script: "docker-compose --project-name ${docker_compose_project}_core -f docker-compose-test.yml ps -q core-test", returnStdout: true).trim()
                sh 'mkdir -p target'
                sh "docker cp ${containerName}:/app/target/test-reports/ target/"
              }
            }
          }
        }

        stage('Test-Agent') {
          when {
            expression {
              return HAVE_AGENT_IMAGE != "0"
            }
          }
          steps {
            script {
              dir("agent") {
                sh 'lein test'
                sh 'rm -f .ssh upspin sign.key'
                sh "docker-compose --project-name ${docker_compose_project}_agent -f test/docker-compose-test.yml --project-directory . rm -v --stop --force"
                sh "docker-compose --project-name ${docker_compose_project}_agent -f test/docker-compose-test.yml --project-directory . up --build --abort-on-container-exit"
                def containerName = sh(script: "docker-compose --project-name ${docker_compose_project}_agent -f test/docker-compose-test.yml --project-directory . ps -q agent-test", returnStdout: true).trim()
                sh "docker cp ${containerName}:/tests/integration.xml target/test-reports/"
                sh "docker-compose --project-name ${docker_compose_project}_agent -f test/docker-compose-test.yml --project-directory . rm --stop --force"
              }
            }
          }
        }

        stage('Test-Web-ui') {
          when {
            expression {
              return HAVE_BANK_A_IMAGE != "0" || HAVE_BANK_B_IMAGE != "0"
            }
          }
          steps {
            script {
              dir("bank-web-ui") {
                sh "NODE_ENV=test yarn unitTest"
                sh "NODE_ENV=test yarn integrationTest"
                // sh "docker-compose --project-name ${docker_compose_project}_bank_ui -f build/docker-compose-test-services.yml --project-directory . rm --force -v"
                // sh "docker-compose --project-name ${docker_compose_project}_bank_ui -f build/docker-compose-test-services.yml --project-directory . up --build --abort-on-container-exit"
                // ui_container = sh(returnStdout: true, script: "docker-compose --project-name ${docker_compose_project}_bank_ui -f build/docker-compose-test-services.yml --project-directory . ps -q web-ui").trim();
                // sh "docker cp ${ui_container}:/work/allTests.xml ."
                // sh "mkdir -vp coverage && docker cp ${ui_container}:/work/coverage/cobertura-coverage.xml coverage/"
                // sh "docker-compose --project-name ${docker_compose_project}_bank_ui -f build/docker-compose-test-services.yml --project-directory . rm --stop --force -v"
              }
            }
          }
        }
      }
    }

    stage('Dockerised test') {
      steps {
        script {
          // Create common jar in sub-projects so we don't rebuild them
          sh 'make -C agent lib/enki/common/0.20.1/common-0.20.1.jar'
          sh 'make -C core lib/enki/common/0.20.1/common-0.20.1.jar'

          // If we've got existing images, pull them in to speed things up
          docker.withRegistry(registry, creds) {
            pull_existing_image(HAVE_BANK_A_IMAGE, "bank-a-web-ui", BANK_WEB_IMAGE_TAG, "web-ui/bank-a.image.latest.stamp")
            pull_existing_image(HAVE_BANK_B_IMAGE, "bank-b-web-ui", BANK_WEB_IMAGE_TAG, "web-ui/bank-b.image.latest.stamp")
            pull_existing_image(HAVE_UPSPIN_KEYSERVER_IMAGE, "upspin-keyserver", UPSPIN_IMAGE_TAG, "upspin/keyserver.image.${UPSPIN_IMAGE_TAG}.stamp")
            pull_existing_image(HAVE_UPSPIN_CLIENT_IMAGE, "upspin-client", UPSPIN_IMAGE_TAG, "upspin/client.image.${UPSPIN_IMAGE_TAG}.stamp")
            pull_existing_image(HAVE_UPSPIN_SERVER_IMAGE, "upspin-server", UPSPIN_IMAGE_TAG, "upspin/server.image.${UPSPIN_IMAGE_TAG}.stamp")
            pull_existing_image(HAVE_AGENT_IMAGE, "agent", AGENT_IMAGE_TAG, "agent/docker-image.latest.stamp")
            pull_existing_image(HAVE_CORE_IMAGE, "core", CORE_IMAGE_TAG, "core/image-core-build.stamp")
          }
          sshagent(['ssh']) {
            sh 'make everything-background'
          }
          sh 'make everything-wait'

          // FIXME: would prefer to run the test suite here, but it would need to be able to know the name of the docker host, which isn't localhost
          // dir('ereshkigal') {
          //   try {
          //     sh 'make check ENKI_SERVER_URL=http://localhost:3000 BANK_URLS="http://localhost:9001 http://localhost:9002"'
          //   } finally {
          //     archiveArtifacts artifacts: "test/ereshkigal/screenshots/**/*.*", allowEmptyArchive: true
          //   }
          // }
        }
      }
      post {
        always {
          script {
            sh 'make everything-down'
          }
        }
      }
    }

    stage('Publish') {
      parallel {
        stage('Publish-Docker') {
          steps {
            script {
              docker.withRegistry(registry, creds) {
                sh "make -C docker -j 4 push-images"
                //if (env.GIT_BRANCH == "master" || env.GIT_BRANCH == "develop") {
                //  images.each {
                //    docker.image("enki/${it}:${escaped_build_tag}").push('latest');
                //  }
                //}
              }
            }
          }
        }

        stage('Publish-Upspin') {
          when {
            expression {
              return HAVE_UPSPIN_CLIENT_IMAGE != "0" || HAVE_UPSPIN_SERVER_IMAGE != "0" || HAVE_UPSPIN_KEYSERVER_IMAGE != "0"
            }
          }
          steps {
            script {
              docker.withRegistry(registry, creds) {
                sh 'make -C upspin push-images'
              }
            }
          }
        }

        stage('Publish-Core') {
          when {
            expression {
              return HAVE_CORE_IMAGE != "0"
            }
          }
          steps {
            script {
              docker.withRegistry(registry, creds) {
                sh "make -C core push-images"
              }
            }
          }
        }

        stage('Publish-Agent') {
          when {
            expression {
              return HAVE_AGENT_IMAGE != "0"
            }
          }
          steps {
            script {
              def imageName = 'enki/agent'
              docker.withRegistry(registry, creds) {
                sh "make -C agent push-images"
              }
            }
          }
        }

        stage('Publish-Demo') {
          steps {
            script {
              docker.withRegistry(registry, creds) {
                sh "make -C demo-suite push-images"
              }
            }
          }
        }

        stage('Publish-Web-UI') {
          when {
            expression {
              return HAVE_BANK_A_IMAGE != "0" || HAVE_BANK_B_IMAGE != "0"
            }
          }
          steps {
            script {
              docker.withRegistry(registry, creds) {
                sh "make -C bank-web-ui push-images"
              }
            }
          }
        }
      }
    }

    stage("Create namespace") {
      when { expression { namespace != null} }
      steps {
        script {
          withCredentials([usernamePassword(credentialsId: 'enki-basic-auth', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
            sh "make -C k8s namespace BASIC_USERNAME=${USER} BASIC_PASSWORD=${PASS}"
          }
        }
      }
    }

    stage("Deploy") {
      parallel {
        stage('Deploy-Core') {
          steps {
            script {
              sh 'mkdir -vp work'
              if (namespace != null) {
                def workdir = "work/jenkins-${env.JOB_NAME.replace('/', '-')}"
                sh("make -C k8s WORKDIR=${WORKSPACE}/${workdir} deploy-core")
                archiveArtifacts "${workdir}/*.yaml"
              } else {
                echo "Cannot determine namespace to deploy to"
              }
            }
          }
        }

        stage('Deploy-Web-UI') {
          steps {
            script {
              if (namespace != null) {
                def delete_arg = ""
                if (git.is_dev_branch() && (env.CHANGE_TITLE.toLowerCase().contains("[clean]"))) {
                  delete_arg = "-d"
                }
                def workdir = "work/jenkins-${env.JOB_NAME.replace('/', '-')}"
                sh "mkdir -vp ${workdir}"
                sh "make -C k8s WORKDIR=${WORKSPACE}/${workdir} deploy-bank-web-ui"
                archiveArtifacts "${workdir}/enki-bank-web-ui.yaml"
              } else {
                echo "Cannot determine namespace to deploy to"
              }
            }
          }
        }

        stage('Deploy-Demo') {
          steps {
            script {
              if (namespace != null) {
                def workdir = "work/deploy-demo-${env.JOB_NAME.replace('/', '-')}"
                sh "mkdir -vp ${workdir}"
                sh "make -C k8s WORKDIR=${WORKSPACE}/${workdir} deploy-demo"
                archiveArtifacts "${workdir}/demo-suite.yaml"
              } else {
                echo "Cannot determine namespace to deploy to"
              }
            }
          }
        }
      }
    }

    stage("Check") {
      parallel {
        stage('Check Deployment-Core') {
          steps {
            script {
              if (namespace != null) {
                withCredentials([usernamePassword(credentialsId: 'enki-basic-auth', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                  sh "make -C k8s await-core BASIC_USERNAME=${USER} BASIC_PASSWORD=${PASS} "
                }
              }
            }
          }
        }

        stage('Check Deployment-Web-UI') {
          steps {
            script {
              if (namespace != null) {
                withCredentials([usernamePassword(credentialsId: 'enki-basic-auth', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                  sh "make -C k8s await-bank-web-ui BASIC_USERNAME=${USER} BASIC_PASSWORD=${PASS} "
                }
              }
            }
          }
        }

        stage('Check Deployment-Demo') {
          steps {
            script {
              if (namespace != null) {
                withCredentials([usernamePassword(credentialsId: 'enki-basic-auth', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                  sh "make -C k8s await-demo BASIC_USERNAME=${USER} BASIC_PASSWORD=${PASS} "
                }
              }
            }
          }
        }
      }
    }

    stage('Trigger smoketest') {
      steps {
        script {
          if (namespace != null) {
            try {
              dir("ereshkigal") {
                withCredentials([usernamePassword(credentialsId: 'enki-basic-auth', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                  sh "make all check VENV=./.venv BASIC_USERNAME=${USER} BASIC_PASSWORD=${PASS} " +
                      "ENKI_SERVER_URL=https://enki.${dns_zone} " +
                      "CORE_HYDRA_URL=https://hydra.${dns_zone} " +
                      "BANK_URLS='https://bank-a.${dns_zone} https://bank-b.${dns_zone}' "
                }
              }
              auth = git.is_dev_branch() ? '' : 'enki:Veyhyptucid5@'
              sh "curl https://" + auth + "demo.${dns_zone}"
            } catch (error) {
              sh "./k8s/kubernetes-show-logs.sh > kubernetes.log"
              archiveArtifacts "kubernetes.log"
              throw error
            } finally {
              archiveArtifacts artifacts: "ereshkigal/test/ereshkigal/screenshots/**/*.*", allowEmptyArchive: true
              archiveArtifacts "ereshkigal/chromedriver.log"
            }
          }
        }
      }
    }

  }

  post {
    success {
      script {
        if (HAVE_AGENT_IMAGE != "0") {
          archive "agent/target/*.jar"
        }
        if (env.GIT_BRANCH == "develop") {
          slackSend color: 'good', message: "Build <${BUILD_URL}|${env.GIT_BRANCH}> of ${JOB_NAME} successful.", channel: '#enki-monitor'
        }
        else {
          echo "On branch ${env.GIT_BRANCH}, so not notifying Slack of success."
        }
      }
    }

    unstable {
      script {
        if (env.GIT_BRANCH == "develop") {
          slackSend color: 'danger', message: "Build <${BUILD_URL}|${env.GIT_BRANCH}> of ${JOB_NAME} unstable", channel: '#enki-monitor'
        }
        else {
          echo "On branch ${env.GIT_BRANCH}, so not notifying Slack of instability"
        }
      }
    }

    failure {
      script {
        if (env.GIT_BRANCH == "develop") {
          slackSend color: 'danger', message: "Build <${BUILD_URL}|${env.GIT_BRANCH}> of ${JOB_NAME} failed", channel: '#enki-monitor'
        }
        else {
          echo "On branch ${env.GIT_BRANCH}, so not notifying Slack of failure"
        }
      }
    }

    always {
      script {
        dir("upspin") {
          sh "docker-compose --project-name ${docker_compose_project}_upspin down || true"
        }

        dir("core") {
          sh "docker-compose --project-name ${docker_compose_project}_core -f docker-compose-test.yml down || true"
        }
        /*
            sh "ls ${dir}/target/test-reports/*.xml || true"
            junit "${dir}/target/test-reports/*.xml"
            sh "ls ${dir}/unitTests.xml || true"
            junit "${dir}/unitTests.xml"
        */
        dir("agent") {
          sh "docker-compose --project-name ${docker_compose_project}_agent -f test/docker-compose-test.yml --project-directory . down || true"
        }

        if (HAVE_AGENT_IMAGE != "0") {
          junit "agent/target/test-reports/*.xml"
        }
  /*
        dir("bank-web-ui") {
          sh "docker-compose --project-name ${docker_compose_project}_bank_ui -f build/docker-compose-test-services.yml --project-directory . down || true"
        }

        junit "bank-web-ui/allTests.xml"
        script {
          if (namespace != null) {
            junit "bank-web-ui/test/ereshkigal/work/junit-report.xml"
          }
        }

        step([$class: 'CoberturaPublisher', coberturaReportFile: "bank-web-ui/coverage/cobertura-coverage.xml"])
  */
      }
    }
  }
}
