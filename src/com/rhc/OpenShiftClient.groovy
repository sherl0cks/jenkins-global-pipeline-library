package com.rhc

import groovy.json.JsonSlurper

def login( String host ){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "oc login ${host} --insecure-skip-tls-verify=true --username=joe --password=redhat"
	commandExecutor.executeInJenkinsOrGroovy( command )
}

def CommandOutput startBuild( String appName, String projectName ){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "oc start-build ${appName} -n ${projectName}"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput getWhoAmI(){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "oc whoami"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput getUserToken(){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "oc whoami -t"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def String getTrimmedUserToken(){
	CommandOutput tokenOutput = getUserToken()
	String tokenStream = tokenOutput.getStandardOut()
	String tokenString = tokenStream.toString()
	String tokenTrimmedString = tokenString.trim()
	return tokenTrimmedString
}

def void waitUntilBuildIsComplete( String buildId, String projectName ){
	long maxMillisecondsToWait = 60000  // TODO make this configurable
	long waitIntervalInMilliseconds = 1000 // TODO make this configurable
	long timeAlreadySpentWaiting = 0
	String trimmedBuildId = buildId.trim()
	while ( true ){
		String status = getBuildStatus( trimmedBuildId, projectName )
		println( "Build ${trimmedBuildId} is ${status}")
		if ( status.equalsIgnoreCase( 'Complete' ) ){
			break
		} else{
			if ( timeAlreadySpentWaiting == maxMillisecondsToWait){
				throw new Exception( "Max interval to poll exceeded - ${maxMillisecondsToWait}" )
			} else {
				timeAlreadySpentWaiting += waitIntervalInMilliseconds
				Thread.sleep( waitIntervalInMilliseconds )
			}
		}
	}
}


def String startBuildAndWaitUntilComplete( String appName, String projectName ){
	CommandOutput buildName = startBuild( appName, projectName )
	waitUntilBuildIsComplete( buildName.standardOut, projectName )
	return buildName.standardOut.trim()
}

def String getBuildStatus( String buildId, String projectName ){
	CommandOutput output = new CommandExecutor().executeInJenkinsOrGroovy( "oc get builds ${buildId} -n ${projectName} -o json" )
	assert output.standardOut != null
	assert !output.standardOut.empty
	assert output.standardErr == null || output.standardErr.empty
	def result = new JsonSlurper().parseText( output.standardOut )
	return result.status.phase
}