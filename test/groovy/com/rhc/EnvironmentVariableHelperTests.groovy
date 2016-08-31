package com.rhc

import org.junit.Before;
import org.junit.Test

class EnvironmentVariableHelperTests {

	EnvironmentVariableHelper helper = new EnvironmentVariableHelper()


	@Test
	void shouldReturnEnvsFromConfig(){
		// given
		def config = getConfig()

		// when
		def result = helper.getEnvsForPipeline( config, null )

		// then
		assert result.equals( getEnvs() )
	}

	@Test
	void shouldReturnEnvsFromEnvironmentVariable(){
		// given
		def config = getConfig()


		// when
		def result = helper.getEnvsForPipeline( config, getEnvironmentStringifiedVariableConfig() )


		// then
		def expectedConfig = getEnvironmentVariableConfig()
		assert result.equals( expectedConfig )
	}
	
	@Test
	void shouldFailWhenNoEnvIsSet(){
		try  {
			helper.getEnvsForPipeline( null, null )
			assert false
		} catch (e){
			assert true
		}
	}

	def getConfig(){
		def config = [ foo: 'bar', a : 'b', envs : [
				[name: 'Dev', projectName: 'infographic-dev'],
				[name: 'Stage', projectName: 'infographic-stage' ],
				[name: 'Production', projectName: 'infographic-prod' ]
			]
		]

		return config
	}

	def getEnvs(){
		def config = [
			[name: 'Dev', projectName: 'infographic-dev'],
			[name: 'Stage', projectName: 'infographic-stage' ],
			[name: 'Production', projectName: 'infographic-prod' ]
		]

		return config
	}

	def getEnvironmentVariableConfig(){
		def envVars = [
			[name: 'Dev', projectName: 'foo-dev'],
			[name: 'Stage', projectName: 'foo-stage' ],
			[name: 'Demo', projectName: 'foo-demo' ]
		]
		return envVars
	}


	def getEnvironmentStringifiedVariableConfig(){
		def envVars = "[\
				[name: 'Dev', projectName: 'foo-dev'],\
				[name: 'Stage', projectName: 'foo-stage' ],\
				[name: 'Demo', projectName: 'foo-demo' ]\
			]"
		return envVars
	}
}
