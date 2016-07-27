# jenkins-global-pipeline-library

This is a repository to be used with [Jenkins Pipeline Global Library Plugin](https://github.com/jenkinsci/workflow-cps-global-lib-plugin). It's inspired by the work in the [fabric8.io community](https://github.com/fabric8io/jenkins-pipeline-library). At some point, as we get more sophisticated, we'll look to fork that repo and merge our changes. For now, here is what we have.

* [Development Pipeline](vars/developmentPipeline.groovy): A reusable outline of a developer's feature branch workflow with OpenShift before opening a pull request for `upstream/master`. Each app's Jenkinsfile must only pass in a few parameters. The flow itself is globally reused.
