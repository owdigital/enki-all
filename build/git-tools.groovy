def is_pull_request() {
  return (JOB_NAME =~ /\/PR-\d+$/);
}

def is_dev_branch() {
  def shared_branches = ["master", "develop"] as Set;
  return !shared_branches.contains(env.BRANCH_NAME)
}

def determine_k8s_namespace(author) {
  switch (env.BRANCH_NAME) {
  // Roll out to production
  case "master":
    echo "Deploys to production are manual for now";
    return null;
  case "develop":
    return "develop"
  default:
    if (is_pull_request() && !author.isAllWhitespace()) {
      return "dev-" + author;
    } else {
      echo "Cannot tell that ${JOB_NAME} is a pull request with a known author, not deploying"
        return null
    }
  }
}

def build_tag() {
   return sh(returnStdout: true, script: "git log -n 1 --pretty=format:%cd-%h --date=format:%Y-%m-%d").trim()
}

def build_author() {
  return sh(returnStdout: true, script: "git log -n 10 --first-parent --pretty=format:'%ae' | grep -E '@(affiliate\\.)?oliverwyman.com\$' | cut -d @ -f 1 | tr . - | head -n 1").trim().toLowerCase()
}

@NonCPS
def isManualBuild() {
  def userCause = currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause);
  return userCause != null
}

@NonCPS
def currentChangesImpactPrefix(String prefix) {
  def changes = currentBuild.changeSets.
    collectMany { changeLog -> Arrays.asList(changeLog.items) }.
    collectMany { entry -> entry.affectedPaths }.
    findAll { file -> file.startsWith(prefix) };

  echo "Changed files in ${prefix}: ${changes}"
  return !changes.isEmpty()
}

@NonCPS
def firstBuildForJob() {
  return currentBuild.previousBuild == null
}

@NonCPS
def shouldRunThisBuild(String prefix) {
  return currentChangesImpactPrefix(prefix) || isManualBuild() || firstBuildForJob()
}

// state is SUCCESS | FAILURE | UNSTABLE | PENDING
def updateGithubCommitStatus(build, state, shouldRun) {

  def message = build.description
  if (!shouldRun) {
    def prevBuild = build.previousBuild;
    state = (prevBuild != null ? prevBuild.result : "SUCCESS");
    message = "using previous build status from ${prevBuild.displayName}: ${state}";
    echo "ShouldRun is ${shouldRun}; using previous build status: ${state}";
  }

  def repoUrl = GIT_URL
  def jobPath = JOB_NAME.split('/')
  def context = Arrays.copyOfRange(jobPath, 0, jobPath.size() - 1).join("/")

  step([
    $class: 'GitHubCommitStatusSetter',
    reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
    contextSource: [$class: "ManuallyEnteredCommitContextSource", context: context],
    errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
    statusResultSource: [
      $class: 'ConditionalStatusResultSource',
      results: [
        [$class: 'AnyBuildResult', state: state, message: message]
      ]
    ]
  ])
}

def hash_folder(name) {
  return sh(script: "git ls-tree HEAD -- ${name} |  awk '{ print \$3 }'", returnStdout: true).trim()
}

def has_image(name, hash) {
  return sh(script: "curl -u _token:${env.ACCESS_TOKEN} --head --fail --silent --show-error https://gcr.io/v2/enki-198710/enki/${name}/manifests/${hash}", returnStatus: true)
}

return this;
