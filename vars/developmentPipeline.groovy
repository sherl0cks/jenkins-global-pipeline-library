import com.rhc.PipelineConfig;

// vars/developmentPipeline.groovy
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def map = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = map
    body()
	
	PipelineConfig config = map as PipelineConfig

    // now build, based on the configuration provided
    def pipelineHelper = new com.rhc.PipelineHelper()

    node {
        stage 'Checkout Code'
        checkout scm

        stage 'Build App'
        pipelineHelper.login( config )
        pipelineHelper.buildApplication( config )

        stage 'Build and Deploy Image'
        pipelineHelper.buildAndDeployImage( config )
        
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
