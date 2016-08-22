package com.rhc

import org.junit.Test


class PipelineConfigTests {
	def map = [:]
	List<EnvConfig> envs

	@Test
	void shouldGetCorrectOpenShiftProjectNameFromEnvironment(){
		envs = new ArrayList<EnvConfig>()
		['Dev', 'Stage', 'Prod'].each { value ->
			EnvConfig e = new EnvConfig( name:value )
			envs.push( e )
		}
		PipelineConfig config = new PipelineConfig( envs:envs, projectName:'infographic' )
		
		assert config.getOpenShiftProjectNameFromEnv( 0 ).equals("infographic-dev")
		assert config.getOpenShiftProjectNameFromEnv( 1 ).equals("infographic-stage")
		assert config.getOpenShiftProjectNameFromEnv( 2 ).equals("infographic-prod")
	}

	@Test
	void shouldCreateDevPipelineConfigFromDevPipeline(){

		def input = {
			buildCommands = ['npm install -only=dev']
			buildTool = 'node-0.10'
			unitTestCommand = 'npm test'
			qualityScanCommand = 'run sonar qube'

			ocHost = 'holmes-playground.demo.innovation.redhat.com'
			dockerRegistry = 'registry.env3-1.innovation.labs.redhat.com'

			projectName = 'infographic'
			appName = 'infographic-node-app'
		}
		input.resolveStrategy = Closure.DELEGATE_FIRST
		input.delegate = map
		input()

		PipelineConfig config = map as PipelineConfig
		assert config != null

		config.getProperties().each { key, value ->
			if ( key in ['class']){
				// skip
			}else if ( value == null ){
				assert map[value] == null
			}
			else {
				def foo = map[key]
				assert foo.equals( value )
			}
		}
	}

	@Test
	void shouldCreateDevPipelineConfigFromReleasePipeline(){

		def input = {
			buildCommands = ['npm install -only=dev']
			buildTool = 'node-0.10'

			ocHost = 'env3-1-master.innovation.labs.redhat.com'
			dockerRegistry = 'registry.env3-1.innovation.labs.redhat.com'

			projectName = 'infographic'
			appName = 'infographic-node-app'

			envs = [
				[name: 'Dev', approvalGroup: 'dev-team'],
				[name: 'Stage', approvalGroup: 'qa-team' ],
				[name: 'Production', approvalGroup: 'biz-team' ]
			]
		}
		input.resolveStrategy = Closure.DELEGATE_FIRST
		input.delegate = map
		input()

		PipelineConfig config = map as PipelineConfig
		assert config != null

		config.getProperties().each { key, value ->
			if ( key in ['class']){
				// skip
			} else {
				if ( value == null ){
					assert map[value] == null
				}
				else if ( value instanceof String || value instanceof String[]){
					def foo = map[key]
					assert foo.equals( value )
				} else {
					assert map[key].size() == value.length
				}
			}
		}
	}
}
