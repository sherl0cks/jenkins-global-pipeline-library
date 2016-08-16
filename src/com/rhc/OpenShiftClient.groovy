package com.rhc

import com.rhc.CommandOutput

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