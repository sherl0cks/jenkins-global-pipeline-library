package com.rhc

class PipelineConfig {

	String[] buildCommands, buildImageCommands 
	String unitTestCommand, buildTool, ocHost, projectName, appName, qualityScanCommand, dockerRegistry
	EnvConfig[] envs
	
	String getOpenShiftProjectNameFromEnv( int i ){
		return projectName.toLowerCase() + '-' +envs[i].name.toLowerCase()
	}
}
