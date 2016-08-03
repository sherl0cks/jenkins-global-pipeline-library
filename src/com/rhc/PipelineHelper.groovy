package com.rhc;

def buildApplication( config ){
    if ( config.appRootContext ){
        dir ( config.appRootContext ){ 
            executeBuildCommands(config)
        }
    } else {
        executeBuildCommands(config)
    }
}

def buildAndDeployImage( config ){
    if ( config.buildImageCommands == null ){
        echo 'No buildImageCommands, using default OpenShift image build and deploy'
        startDefaultOpenShiftBuildAndDeploy( config )
    } else {
        echo 'Found buildImageCommands, executing in shell'
        executeListOfShellCommands( config )
    }
}

def getBuildTools(){
	return ['node-0.10', 'Maven3.3.9'];
}

def startDefaultOpenShiftBuildAndDeploy( config ){
    def oc = new OpenShiftClient()
    oc.login( config.ocHost )
    oc.startBuild( config.appName, config.projectName )
}

def executeBuildCommands( config ){
    def tools = getBuildTools()

    if ( config.buildTool == null ){
        echo 'No build tool declared. Any commands will execute directly in the shell.'
        executeListOfShellCommands( config )
    } else if ( config.buildTool in tools ) {
        echo "Using build tool: ${config.buildTool}"
        executeListOfToolCommands( config )
    } else {
        error "${config.buildTool} is currently unsupported. Please select one of ${tools}."
    }
}

def executeListOfShellCommands( config ){
    for (int i=0; i<config.buildCommands.size(); i++){
        def command = config.buildCommands[i]
        sh "${command}" 
    }
}

def executeListOfToolCommands( config ){
    def toolHome = tool "${config.buildTool}"
    for (int i=0; i<config.buildCommands.size(); i++){
        def command = config.buildCommands[i]
        sh "${toolHome}/bin/${command}" 
    }
}