package com.rhc

def CommandOutput login( String host, String userToken ){
	def commandExecutor = new CommandExecutor();
	def command = "docker login -u='joe' -e='jholmes@redhat.com' -p=${userToken} ${host}"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput pushOrPull( String operation, String repositoryString ){
	def commandExecutor = new CommandExecutor();
	def command = "docker ${operation} ${repositoryString}"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput pull( String repositoryString ){
	return pushOrPull( 'pull', repositoryString )
}

def CommandOutput push( String repositoryString ){
	return pushOrPull( 'push', repositoryString )
}

def CommandOutput tagImage( String imageId, String repositoryStringWithVersion ){
	def String dockerVersion = getDockerClientVersionTrimmedString()
	if (imageId == null || imageId.empty ){
		println( 'imageId is empty' )
	}
	return tagImage( imageId, repositoryStringWithVersion, dockerVersion )
}

def CommandOutput tagImage( String imageId, String repositoryStringWithVersion, String dockerVersion ){
	def oneNinePattern = getDockerOneNineRegex()
	def command
	if ( dockerVersion =~ oneNinePattern ){
		command = "docker tag -f ${imageId} ${repositoryStringWithVersion}" // this is not unit tested currently
	} else {
		command = "docker tag ${imageId} ${repositoryStringWithVersion}"
	}
	def commandExecutor = new CommandExecutor();
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput retrieveImageId( String repositoryStringWithVersion, String dockerVersion ){
	def oneNinePattern = getDockerOneNineRegex()
	def command
	def legacyAPI = false
	if ( dockerVersion =~ oneNinePattern ){
		command = "docker images ${repositoryStringWithVersion}"
		legacyAPI = true
	} else {
		command = "docker images --format '{{.ID}}' ${repositoryStringWithVersion}"
	}
	def commandExecutor = new CommandExecutor();
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	if ( dockerVersion =~ oneNinePattern ){
		def outputString = output.standardOut
		def outputTokens = outputString.split( '\\s+' )
		def boolean found = false
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
	def String dockerVersion = getDockerClientVersionTrimmedString()
	return retrieveImageId( repositoryStringWithVersion, dockerVersion )
}

def CommandOutput promoteImageBetweenRepositories( String currentRepositoryWithVersion, String newRepositoryWithVersion ){
	pull( currentRepositoryWithVersion )
	def CommandOutput imageId = retrieveImageId( currentRepositoryWithVersion )
	tagImage( trimImageId(imageId.standardOut), newRepositoryWithVersion )
	def CommandOutput commandOutput = push( newRepositoryWithVersion )
	return commandOutput
}

def String trimImageId( String imageIdString ){
	def String trimmedString = imageIdString.trim()
	if ( trimmedString.length() > 12 ){
		def length = trimmedString.length()
		length -= 1
		return trimmedString.trim().substring(1, length)
	} else {
		return trimmedString
	}
}

def CommandOutput getDockerClientVersion(){
	def commandExecutor = new CommandExecutor();
	def command = "docker -v"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def String getDockerClientVersionTrimmedString(){
	def CommandOutput output = getDockerClientVersion()

	def tokens = output.standardOut.split(' ')
	def versionToken = tokens[2]
	def versionString = versionToken.substring(0, versionToken.size()-1)

	return versionString
}

def String buildRepositoryString( String host, String projectName, String imageName ){
	return "${host}/${projectName}/${imageName}"
}

def String buildRepositoryStringWithVersion( String host, String projectName, String imageName, String version ){
	def repositoryString = buildRepositoryString( host, projectName, imageName )
	return "${repositoryString}:${version}"
}

def getDockerOneTenImageIdRegex(){
	return ~/[0-9a-f]{12}/
}

def getDockerOneNineImageIdRegex(){
	return ~/sha256:[0-9a-f]{5}/
}

def getImageIdRegex(){
	if ( getDockerClientVersionTrimmedString() =~ getDockerOneNineRegex() ){
		return getDockerOneNineImageIdRegex()
	} else {
		return getDockerOneTenImageIdRegex()
	}
}
def getDockerOneNineRegex(){
	return ~/1.9.[0-9]+/
}