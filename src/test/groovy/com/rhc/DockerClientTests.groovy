package com.rhc

import java.util.regex.Pattern

import org.junit.BeforeClass
import org.junit.Test

class DockerClientTests {

	static String dockerRegistryHost = 'registry.env3-1.innovation.labs.redhat.com'
	static String openShiftHost = 'env3-1-master.innovation.labs.redhat.com'
	static String projectName = 'holmes-stage'
	static String newProjectName = 'holmes-prod'
	static String imageName = 'infographic-node-app'
	static DockerClient dockerClient = new DockerClient()
	static OpenShiftClient openShiftClient = new OpenShiftClient()


	@BeforeClass
	static void shouldLoginToRegistry(){
		openShiftClient.login( openShiftHost )
		CommandOutput output = dockerClient.login( dockerRegistryHost, openShiftClient.getTrimmedUserToken() )
		assert output.standardOut.contains( 'Login Succeeded' )
	}

	@Test
	void shouldFailToPullImage(){
		CommandOutput output = dockerClient.pull( getRepositoryBadString() )
		assert output.standardErr.contains( 'not found' )
	}

	@Test
	void shouldSuccessfullyPullImage(){
		CommandOutput output = dockerClient.pull( getRepositoryGoodString() )
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}

	@Test
	void shouldFailToPushImage(){
		CommandOutput output = dockerClient.push( getRepositoryBadString() )
		assert output.standardErr.contains( 'Repository does not exist' )
	}

	@Test
	void shouldSuccessfullyPushImage(){
		CommandOutput output = dockerClient.push( getRepositoryGoodString( ))
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}

	@Test
	void shouldFailToTagImage(){
		CommandOutput output = dockerClient.tagImage( '1234', getRepositoryBadStringWithVersion() )
		assert output.standardErr.toLowerCase().contains( 'no such id: 1234' )
	}

	@Test
	void shouldSuccessfullyTagImage(){
		CommandOutput imageId = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion( ) )
		CommandOutput output = dockerClient.tagImage( dockerClient.trimImageId( imageId.standardOut ), getRepositoryGoodStringWithVersion() )
		assert output.standardErr.empty
		assert output.standardOut.empty
	}

	@Test
	void shouldFailToRetrieveImageId(){
		CommandOutput output = dockerClient.retrieveImageId( getRepositoryBadStringWithVersion() )
		assert output.standardErr.empty
		assert output.standardOut.empty
	}

	@Test
	void shouldSucessfullyRetrieveImageId(){
		CommandOutput output = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion() )
		Pattern pattern = dockerClient.getImageIdRegex()
		assert output.standardErr.empty
		assert dockerClient.trimImageId( output.standardOut ) ==~ pattern
	}

	@Test
	void shouldSucessfullyRetrieveImageIdWithLegacyFormat(){
		CommandOutput output = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion(), '1.9.0' )
		Pattern pattern = dockerClient.getImageIdRegex()
		assert output.standardErr.empty
		assert dockerClient.trimImageId( output.standardOut ) ==~  pattern
	}

	@Test
	void shouldFailToRetrieveImageIdWithLegacyFormat(){
		CommandOutput output = dockerClient.retrieveImageId( getRepositoryBadStringWithVersion(), '1.9.0' )
		assert output.standardErr.empty
		assert output.standardOut.empty
	}

	@Test
	void shouldSuccessfullyPromoteImageBetweenRepositories(){
		String currentRepoString = getRepositoryGoodStringWithVersion()
		String newRepoString = dockerClient.buildRepositoryStringWithVersion( dockerRegistryHost, newProjectName, imageName, 'latest' )
		CommandOutput output = dockerClient.promoteImageBetweenRepositories( currentRepoString, newRepoString )
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}

	@Test
	void shouldTrimImageString(){
		CommandOutput imageId = dockerClient.retrieveImageId( getRepositoryGoodStringWithVersion() )
		String trimmedImageIdString = dockerClient.trimImageId( imageId.standardOut )
		Pattern pattern = ~/[0-9a-f]+/
		assert trimmedImageIdString =~ pattern
	}

	@Test
	void shouldGetDockerVersion(){
		CommandOutput output = dockerClient.getDockerClientVersion()
		assert output.standardErr.isEmpty()
		assert !output.standardOut.isEmpty()
		assert output.standardOut.toLowerCase().contains( 'version' )
	}

	@Test
	void shouldGetDockerVersionTrimmedString(){
		String versionString = dockerClient.getDockerClientVersionTrimmedString()
		Pattern pattern = ~/[0-9]+.[0-9]+.[0-9]+/
		assert versionString =~ pattern
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
