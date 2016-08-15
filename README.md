# Jenkins Global Pipeline Library

This is a repository to be used with [Jenkins Pipeline Global Library Plugin](https://github.com/jenkinsci/workflow-cps-global-lib-plugin). It's inspired by the work in the [fabric8.io community](https://github.com/fabric8io/jenkins-pipeline-library). At some point, as we get more sophisticated, we'll look to fork that repo and merge our changes. For now, here is what we have.


* [Release Pipeline](vars/releasePipeline.groovy): A reusable skeletal framework for a continuous delivery pipeline. Currently modelled from a commit is made on the master branch of a central repository (preferably via a peer reviewed pull request) through a production deployment. The current implementation defaults to a container driven approach using docker and OpenShift; however, the implementation is pluggable and we welcome collaboration on now docker/OpenShift use cases. A very simple set of parameters is need to configure this generic pipeline to be used for the application itself, as can be seen in our [node.js example](https://github.com/rht-labs/infographic-node-app/blob/master/ReleaseJenkinsfile.groovy).

* [Development Pipeline](vars/developmentPipeline.groovy): EXPERIMENTAL - A reusable outline of a developer's feature branch workflow with OpenShift before opening a pull request for to a central, master branch. A very simple set of parameters is need to configure this generic pipeline to be used for the application itself, as can be seen in our [node.js example](https://github.com/rht-labs/infographic-node-app/blob/master/DevJenkinsfile.groovy).

## Getting Started

- Clone the project
- Use the [gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to install the right version of Groovy and Gradle
- Currently the tests (e.g. `gradle test`) require access to test infrastructure that we have running behind the Red Hat network. We'll look to change this in the future

## Contributing

- Open an issue here on Github so we know you are interested in a certain feature or function
- Fork the repo
- Submit a PR that fixes the issue

## License
[ASL2](LICENSE)