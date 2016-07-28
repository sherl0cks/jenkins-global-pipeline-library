// vars/developmentPipeline.groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // now build, based on the configuration provided
    node {
        
        buildSnapshotStage( config )
        
        deploySnapshotStage( config )

        stage 'smoke test snapshot'

        stage 'unit test'
        echo "${config.unitTestCommand}"
    }
    
    stage concurrency: 3, name: 'slow tests'
    parallel 'quality scan': {
        node {
           echo "${config.qualityScanCommand}"
        }
    }, 'integration test': {
        node {
            echo "${config.integrationTestCommand}"
        }
    }, 'acceptance test': {
        node {
            echo "${config.acceptanceTestCommand}"
        }    
    }, failFast: true
}

def getOpenShiftAppName(){
    return "name possibly from Jenkins Job?"
}

def buildSnapshotStage( config ){
    stage 'build snapshot'
    checkout scm
    
    if ( config.appRootContext ){
        dir ( config.appRootContext ){   
            buildSnapshot(config)
        }
    } else {
        buildSnapshot(config)
    }   
}

def buildSnapshot(config){

    def tools = ['node-0.10', 'Maven3.3.9']

    if ( config.buildTool == null ){
        echo 'No build tool declared. Any commands will execute directly in the shell.'
        for (int i=0; i<config.buildCommands.size(); i++){
            def command = config.buildCommands[i]
            sh "${command}" 
        }
    } else if ( config.buildTool in tools ) {
        def toolHome = tool "${config.buildTool}"
        for (int i=0; i<config.buildCommands.size(); i++){
            def command = config.buildCommands[i]
            sh "${toolHome}/bin/${command}" 
        }
    } else {
        error "${config.buildTool} is currently unsupported. Please select one of ${tools}."
    }
}

def deploySnapshotStage( config ){
    stage 'deploy snapshot'

    sh "oc login ${config.ocHost} --insecure-skip-tls-verify=true --username=joe --password=redhat"
    sh "oc project ${config.projectName}"
    sh "oc start-build ${config.appName}" //-e ARTIFACT_URL=$hi"
}