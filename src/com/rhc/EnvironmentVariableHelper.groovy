package com.rhc

class EnvironmentVariableHelper {

	public static final OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS = 'OPENSHIFT_PROJECTS'

	// If environment variable is set properly, use it over what is passed in the config
	def getEnvsFromPipeline( def config){
		def envProperty = System.getProperty( OVERRIDE_SYS_PROPERTY_FOR_ENVIRONMENTS )
		if ( envProperty != null ) {
			println 'hi'
			
			def envVarConfig =  new GroovyShell().evaluate( envProperty )
			
			return envVarConfig
		} else {
			return config
		}
	}
}
