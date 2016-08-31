// vars/releasePipeline.groovy
def call( body ) {
	// evaluate the body block, and collect configuration into the object
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def pipelineHelper = new com.rhc.PipelineHelper()


	node {
		// This a simple dump of the env to help with debugging
		sh 'env'
		
		stage 'Code Checkout'
		checkout scm

		stage 'Build App'
		pipelineHelper.login( config )
		pipelineHelper.buildApplication( config )

		stage 'Run Tests'
		echo "We aren't doing anything here yet" // TODO figure out how to handle this

		stage 'Build Image and Deploy to Dev'
		pipelineHelper.buildAndDeployImage( config )

		
		
	}
	node {
		def envs = pipelineHelper.getEnvsForPipeline( config, "${env.OPENSHIFT_PROJECTS}" )
		println envs
		
		// Loop for environments
		for (int i=1; i<envs.size(); i++){
			def envName = envs[i].name
			def deployCommands = envs[i].deployCommands
			stage "Deploy to ${envName}"
			input "Deploy to ${envName}?"
			if ( deployCommands != null ){
				pipelineHelper.executeListOfShellCommands( deployCommands )
				println( deployCommands )
			} else {
				pipelineHelper.promoteImage( envs[i-1], envs[i], config )
			}
		}

	}

}