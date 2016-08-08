package com.rhc

import org.junit.BeforeClass;
import org.junit.Test

class DockerClientTests {
	
	def static dockerRegistryHost = 'registry.env3-1.innovation.labs.redhat.com'
	def static openShiftHost = 'env3-1-master.innovation.labs.redhat.com'
	def static projectName = 'holmes-stage'
	def static newProjectName = 'holmes-prod'
	def static imageName = 'infographic-node-app'
	def static dockerClient = new DockerClient()
	def static openShiftClient = new OpenShiftClient()

	
	@BeforeClass
	static void shouldLoginToRegistry(){
		openShiftClient.login( openShiftHost )
		def CommandOutput output = dockerClient.login( dockerRegistryHost, openShiftClient.getTrimmedUserToken() )
		assert output.standardOut.contains( 'Login Succeeded' )
	}
	
	@Test
	void shouldFailToPullImage(){
		def CommandOutput output = dockerClient.pull( getRepositoryBadString() )
		assert output.standardErr.contains( 'not found' )
	}
	
	@Test
	void shouldSuccessfullyPullImage(){
		def CommandOutput output = dockerClient.pull( getRepositoryGoodString() )
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}
	
	@Test
	void shouldFailToPushImage(){
		def CommandOutput output = dockerClient.push( getRepositoryBadString() )
		assert output.standardErr.contains( 'Repository does not exist' )
	}
	
	@Test
	void shouldSuccessfullyPushImage(){
		def CommandOutput output = dockerClient.push( getRepositoryGoodString( ))
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}
	
	@Test
	void shouldFailToTagImage(){
		def CommandOutput output = dockerClient.tagImage( '1234', getRepositoryBadStringWithVersion() )
		assert output.standardErr.toLowerCase().contains( 'no such id: 1234' )
	}
	
	@Test
	void shouldSuccessfullyTagImage(){
		def CommandOutput output = dockerClient.tagImage( 'cca3762c12a3', getRepositoryGoodStringWithVersion() )
		assert output.standardErr.empty
		assert output.standardOut.empty
	}
	
	@Test
	void shouldFailToRetrieveImageId(){
		def CommandOutput output = dockerClient.retrieveImageId( getRepositoryBadStringWithVersion() )
		assert output.standardErr.empty
		assert output.standardOut.empty
	}
	
	@Test
	void shouldSucessfullyRetrieveImageId(){
		def CommandOutput output = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion() )
		assert output.standardErr.empty
		assert !output.standardOut.empty
	}
	
	@Test
	void shouldSuccessfullyPromoteImageBetweenRepositories(){
		def currentRepoString = getRepositoryGoodStringWithVersion()
		def newRepoString = dockerClient.buildRepositoryStringWithVersion( dockerRegistryHost, newProjectName, imageName, 'latest' )
		def CommandOutput output = dockerClient.promoteImageBetweenRepositories( currentRepoString, newRepoString )
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}
	
	@Test
	void shouldTrimImageString(){
		def CommandOutput imageId = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion() ) 
		def String trimmedImageIdString = dockerClient.trimImageId( imageId.standardOut )
		def pattern = ~/[0-9a-f]+/
		assert trimmedImageIdString =~ pattern
	}
	
	String getRepositoryGoodString(){
		return dockerClient.buildRepositoryString( dockerRegistryHost, projectName, imageName )
	}
	
	String getRepositoryGoodStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( dockerRegistryHost, projectName, imageName, 'latest' )
	}
	
	String getRepositoryBadString(){
		return dockerClient.buildRepositoryString( dockerRegistryHost, projectName, 'foobar' )
	}
	
	String getRepositoryBadStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( dockerRegistryHost, projectName, 'foobar', 'latest' )
	}
}
