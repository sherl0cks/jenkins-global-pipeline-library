package com.rhc

import java.util.regex.Pattern

def CommandOutput login( String host, String userToken ){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "docker login -u='joe' -e='jholmes@redhat.com' -p=${userToken} ${host}"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput pushOrPull( String operation, String repositoryString ){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "docker ${operation} ${repositoryString}"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput pull( String repositoryString ){
	return pushOrPull( 'pull', repositoryString )
}

def CommandOutput push( String repositoryString ){
	return pushOrPull( 'push', repositoryString )
}

def CommandOutput tagImage( String imageId, String repositoryStringWithVersion ){
	String dockerVersion = getDockerClientVersionTrimmedString()
	if (imageId == null || imageId.empty ){
		println( 'imageId is empty' )
	}
	return tagImage( imageId, repositoryStringWithVersion, dockerVersion )
}

def CommandOutput tagImage( String imageId, String repositoryStringWithVersion, String dockerVersion ){
	Pattern oneNinePattern = getDockerOneNineRegex()
	String command
	if ( dockerVersion =~ oneNinePattern ){
		command = "docker tag -f ${imageId} ${repositoryStringWithVersion}" // this is not unit tested currently
	} else {
		command = "docker tag ${imageId} ${repositoryStringWithVersion}"
	}
	CommandExecutor commandExecutor = new CommandExecutor();
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput retrieveImageId( String repositoryStringWithVersion, String dockerVersion ){
	Pattern oneNinePattern = getDockerOneNineRegex()
	String command
	boolean legacyAPI = false
	if ( dockerVersion =~ oneNinePattern ){
		command = "docker images ${repositoryStringWithVersion}"
		legacyAPI = true
	} else {
		command = "docker images --format '{{.ID}}' ${repositoryStringWithVersion}"
	}
	CommandExecutor commandExecutor = new CommandExecutor();
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	if ( dockerVersion =~ oneNinePattern ){
		String outputString = output.standardOut
		String[] outputTokens = outputString.split( '\\s+' )
		boolean found = false
		for ( int i=0; i<outputTokens.length; i++ ){
			if ( ( outputTokens[i] =~ getDockerOneTenImageIdRegex() ) || outputTokens[i] =~ getDockerOneNineImageIdRegex() ) {
				output.standardOut = outputTokens[i]
				found = true
			}
		}
		if ( found == false ){
			output.standardOut = ''
		}
	}
	return output
}

def CommandOutput retrieveImageId( String repositoryStringWithVersion ){
	String dockerVersion = getDockerClientVersionTrimmedString()
	return retrieveImageId( repositoryStringWithVersion, dockerVersion )
}

def CommandOutput promoteImageBetweenRepositories( String currentRepositoryWithVersion, String newRepositoryWithVersion ){
	pull( currentRepositoryWithVersion )
	CommandOutput imageId = retrieveImageId( currentRepositoryWithVersion )
	tagImage( trimImageId(imageId.standardOut), newRepositoryWithVersion )
	CommandOutput commandOutput = push( newRepositoryWithVersion )
	return commandOutput
}

def String trimImageId( String imageIdString ){
	String trimmedString = imageIdString.trim()
	if ( trimmedString.length() > 12 ){
		def length = trimmedString.length()
		length -= 1
		return trimmedString.trim().substring(1, length)
	} else {
		return trimmedString
	}
}

def CommandOutput getDockerClientVersion(){
	CommandExecutor commandExecutor = new CommandExecutor();
	String command = "docker -v"
	CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def String getDockerClientVersionTrimmedString(){
	CommandOutput output = getDockerClientVersion()

	String[] tokens = output.standardOut.split(' ')
	String versionToken = tokens[2]
	String versionString = versionToken.substring(0, versionToken.size()-1)

	return versionString
}

def String buildRepositoryString( String host, String projectName, String imageName ){
	return "${host}/${projectName}/${imageName}"
}

def String buildRepositoryStringWithVersion( String host, String projectName, String imageName, String version ){
	def repositoryString = buildRepositoryString( host, projectName, imageName )
	return "${repositoryString}:${version}"
}

def Pattern getDockerOneTenImageIdRegex(){
	return ~/[0-9a-f]{12}/
}

def Pattern getDockerOneNineImageIdRegex(){
	return ~/sha256:[0-9a-f]{5}/
}

def getImageIdRegex(){
	if ( getDockerClientVersionTrimmedString() =~ getDockerOneNineRegex() ){
		return getDockerOneNineImageIdRegex()
	} else {
		return getDockerOneTenImageIdRegex()
	}
}
def Pattern getDockerOneNineRegex(){
	return ~/1.9.[0-9]+/
}