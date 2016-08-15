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

	}

}