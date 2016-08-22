package com.rhc


import java.util.regex.Pattern

import org.junit.BeforeClass
import org.junit.Test

import groovy.transform.TypeChecked;


@TypeChecked
class DockerClientTests {

	static final String DOCKER_REGISTRY_HOST = 'registry.env3-1.innovation.labs.redhat.com'
	static final String OPENSHIFT_HOST = 'env3-1-master.innovation.labs.redhat.com'
	static final String PROJECT_NAME = 'holmes-stage'
	static final String NEW_PROJECT_NAME = 'holmes-prod'
	static final String IMAGE_NAME = 'infographic-node-app'
	static final DockerClient dockerClient = new DockerClient()
	static final OpenShiftClient openShiftClient = new OpenShiftClient()


	@BeforeClass
	static void shouldLoginToRegistry(){
		openShiftClient.login( OPENSHIFT_HOST )
		CommandOutput output = dockerClient.login( DOCKER_REGISTRY_HOST, openShiftClient.getTrimmedUserToken() )
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
		String newRepoString = dockerClient.buildRepositoryStringWithVersion( DOCKER_REGISTRY_HOST, NEW_PROJECT_NAME, IMAGE_NAME, 'latest' )
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
		return dockerClient.buildRepositoryString( DOCKER_REGISTRY_HOST, PROJECT_NAME, IMAGE_NAME )
	}

	String getRepositoryGoodStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( DOCKER_REGISTRY_HOST, PROJECT_NAME, IMAGE_NAME, 'latest' )
	}

	String getRepositoryBadString(){
		return dockerClient.buildRepositoryString( DOCKER_REGISTRY_HOST, PROJECT_NAME, 'foobar' )
	}

	String getRepositoryBadStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( DOCKER_REGISTRY_HOST, PROJECT_NAME, 'foobar', 'latest' )
	}
}
