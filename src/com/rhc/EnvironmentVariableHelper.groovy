package com.rhc

class EnvironmentVariableHelper {

	public static final OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS = 'OPENSHIFT_PROJECTS'

	// If environment variable is set properly, use it over what is passed in the config
	def getEnvsForPipeline( config, envVarString ){
		if ( envVarString != null ) {
			println 'Found OPENSHIFT_PROJECTS env var. Overriding environments declared in Jenkinsfile'			
			def envVarConfig =  new GroovyShell().evaluate( envVarString )
			return envVarConfig
		} else {
			println 'No OPENSHIFT_PROJECTS env var found'
			return config.envs
		}
	}
}
