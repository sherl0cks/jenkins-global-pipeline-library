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
	def commandExecutor = new CommandExecutor();
	def command = "docker tag ${imageId} ${repositoryStringWithVersion}"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
}

def CommandOutput retrieveImageId( String repositoryStringWithVersion ){
	def commandExecutor = new CommandExecutor();
	def command = "docker images --format '{{.ID}}' ${repositoryStringWithVersion}"
	def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
	return output
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

def String buildRepositoryString( String host, String projectName, String imageName ){
	return "${host}/${projectName}/${imageName}"
}

def String buildRepositoryStringWithVersion( String host, String projectName, String imageName, String version ){
	def repositoryString = buildRepositoryString( host, projectName, imageName )
	return "${repositoryString}:${version}"
}
