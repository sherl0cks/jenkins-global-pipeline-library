package com.rhc

import com.rhc.CommandOutput

def login( String host ){
	def commandExecutor = new CommandExecutor();
	def command = "oc login ${host} --insecure-skip-tls-verify=true --username=joe --password=redhat"
	commandExecutor.executeInJenkinsOrGroovy( command )
}

def CommandOutput startBuild( String appName, String projectName ){
	def commandExecutor = new CommandExecutor();
	def command = "oc start-build ${appName} -n ${projectName}"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput getWhoAmI(){
	def commandExecutor = new CommandExecutor();
	def command = "oc whoami"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput getUserToken(){
	def commandExecutor = new CommandExecutor();
	def command = "oc whoami -t"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def String getTrimmedUserToken(){
	def CommandOutput tokenOutput = getUserToken()
	def tokenStream = tokenOutput.getStandardOut()
	def tokenString = tokenStream.toString()
	def tokenTrimmedString = tokenString.trim()
	return tokenTrimmedString
}