package com.rhc

import org.junit.Before;
import org.junit.Test

class EnvironmentVariableHelperTests {

	EnvironmentVariableHelper helper = new EnvironmentVariableHelper()


	@Before
	void before() {
		clearEnvironmentVariables()
	}

	@Test
	void shouldReturnEnvsFromConfig(){
		// given
		def config = getConfig()

		// when
		def result = helper.getEnvsFromPipeline( config )

		// then
		def expectedConfig = getConfig()
		assert result.equals( expectedConfig )
	}

	@Test
	void shouldReturnEnvsFromEnvironmentVariable(){
		// given
		def config = getConfig()
		setEnvironmentVariables()


		// when
		def result = helper.getEnvsFromPipeline( config )


		// then
		def expectedConfig = getEnvironmentVariableConfig()
		assert result.equals( expectedConfig )
	}

	void setEnvironmentVariables(){
		def envVars = getEnvironmentVariableConfig()
		System.setProperty( EnvironmentVariableHelper.OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS, getEnvironmentStringifiedVariableConfig()  )
	}

	void clearEnvironmentVariables(){
		def foo = System.getProperty(EnvironmentVariableHelper.OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS)
		if ( foo != null ){
			System.getProperties().remove( EnvironmentVariableHelper.OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS )
		}
	}

	def getConfig(){
		def config =  [ envs : [
				[name: 'Dev', projectName: 'infographic-dev'],
				[name: 'Stage', projectName: 'infographic-stage' ],
				[name: 'Production', projectName: 'infographic-prod' ]
			]
		]
		return config
	}

	def getEnvironmentVariableConfig(){
		def envVars = [ envs : [
				[name: 'Dev', projectName: 'foo-dev'],
				[name: 'Stage', projectName: 'foo-stage' ],
				[name: 'Demo', projectName: 'foo-demo' ]
			]
		]
		return envVars
	}
	
	
	def getEnvironmentStringifiedVariableConfig(){
		def envVars = "[ envs : [\
				[name: 'Dev', projectName: 'foo-dev'],\
				[name: 'Stage', projectName: 'foo-stage' ],\
				[name: 'Demo', projectName: 'foo-demo' ]\
			]\
		]"
		return envVars
	}
}
