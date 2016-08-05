// vars/releasePipeline.groovy
def call( body ) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def pipelineHelper = new com.rhc.PipelineHelper()
	
	

	node {

	stage 'Code Checkout'
	checkout scm

	stage 'Build App'
	pipelineHelper.login( config ) 
	pipelineHelper.buildApplication( config )

	stage 'Run Tests'
	echo "We aren't doing anything here yet" // TODO figure out how to handle this

	stage 'Build Image and Deploy to Dev'
	pipelineHelper.buildAndDeployImage( config )
	
	// Loop for environments
	for (int i=1; i<config.envs.size(); i++){
		def envName = config.envs[i].name
		def deployCommands = config.envs[i].deployCommands
		stage "Deploy to ${envName}"
		input "Deploy to ${envName}?"
		if ( deployCommands != null ){
			pipelineHelper.executeListOfShellCommands( deployCommands )
			println( deployCommands )
		} else {
			pipelineHelper.promoteImage( config.envs[i-1], config.envs[i], config )
		}
	}

/*
	   // Configure FHC&#10;
	   stage &apos;Configure fhc&apos;&#10;
	   sh &apos;fhc target {{ rhmap.domain }}&apos;&#10;
	   sh &apos;fhc login {{ rhmap.username }} {{ rhmap.password }}&apos;&#10;
	   sh &apos;fhc fhcfg set fhversion 3&apos;&#10;
	   sh &apos;fhc fhcfg set version 3&apos;&#10;
	&#10;
	&#10;
	   // Trigger cloud git pull&#10;
	   stage &apos;Trigger cloud git pull&apos;&#10;
	   sh &apos;/usr/local/bin/fhc git pull {{ item.response.guid }}&apos;&#10;
	&#10;
	&#10;
	   // Trigger opp stage&#10;
	   stage &apos;Trigger cloud app stage&apos;&#10;
	   sh &apos;/usr/local/bin/fhc app stage --app={{ item.response.guid }} --env={{ engagement_name }}-development&apos;&#10;
	&#10;
	&#10;
	}&#10;
*/
	}

}