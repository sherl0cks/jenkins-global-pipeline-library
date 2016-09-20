package com.rhc

import java.util.regex.Pattern

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
/**
 * These tests target the Red Hat CDK. You need to make sure you local host has added the docker cert from CDK
 */
class DockerClientTests {

	static final String DOCKER_REGISTRY_HOST = 'hub.openshift.rhel-cdk.10.1.2.2.xip.io'
	static final String OPENSHIFT_HOST = '10.1.2.2:8443'
	static final String PROJECT_NAME = 'sample-project'
	static final String NEW_PROJECT_NAME = 'sample-project-2'
	static final String IMAGE_NAME = 'ruby-ex'
	static DockerClient dockerClient = new DockerClient()
	static OpenShiftClient openShiftClient = new OpenShiftClient()
	static final String USER_NAME = "admin"
	static final String PASSWORD = "admin"


	@BeforeClass
	static void shouldLoginToRegistry(){
		openShiftClient.login( OPENSHIFT_HOST, USER_NAME, PASSWORD )
		CommandOutput output = dockerClient.login( DOCKER_REGISTRY_HOST, openShiftClient.getTrimmedUserToken() )
		assert output.standardOut.contains( 'Login Succeeded' )
		CommandOutput output2 = new CommandExecutor().executeInJenkinsOrGroovy( "oc new-project ${NEW_PROJECT_NAME}" )
		assert output2.standardOut.contains( "Now using project \"${NEW_PROJECT_NAME}\" on server" )
		CommandOutput output3 = dockerClient.pull( getRubyRepositoryStringWithVersion() )
	}
	
	@AfterClass
	static void cleanUp(){
		CommandOutput output = new CommandExecutor().executeInJenkinsOrGroovy( "oc delete project ${NEW_PROJECT_NAME}" )
		assert output.standardOut.contains( "project \"${NEW_PROJECT_NAME}\" deleted")
	}


	@Test
	void shouldFailToPullImage(){
		CommandOutput output = dockerClient.pull( getRepositoryBadString() )
		assert output.standardErr.contains( 'not found' )
	}

	@Test
	void shouldSuccessfullyPullImage(){
		CommandOutput output = dockerClient.pull( getRubyRepositoryString() )
		assert output.standardOut.toLowerCase().contains( 'digest' )
	}

	@Test
	void shouldFailToPushImage(){
		CommandOutput output = dockerClient.push( getRepositoryBadString() )
		assert output.standardErr.contains( 'Repository does not exist' )
	}

	@Test
	void shouldFailToTagImage(){
		CommandOutput output = dockerClient.tagImage( '1234', getRepositoryBadStringWithVersion() )
		assert output.standardErr.toLowerCase().contains( 'no such id: 1234' )
	}

	@Test
	void shouldSuccessfullyTagImage(){
		CommandOutput imageId = dockerClient.retrieveImageId( getRubyRepositoryStringWithVersion( ) )
		CommandOutput output = dockerClient.tagImage( dockerClient.trimImageId( imageId.standardOut ), getRubyRepositoryStringWithVersion() )
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
		CommandOutput output = dockerClient.retrieveImageId( getRubyRepositoryStringWithVersion() )
		Pattern pattern = dockerClient.getImageIdRegex()
		assert output.standardErr.empty
		assert output.standardOut != null
		println output.standardOut
		assert dockerClient.trimImageId( output.standardOut ) ==~ pattern
	}

	@Test
	void shouldSucessfullyRetrieveImageIdWithLegacyFormat(){
		CommandOutput output = dockerClient.retrieveImageId( getRubyRepositoryStringWithVersion(), '1.9.0' )
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
		CommandOutput imageId = dockerClient.retrieveImageId( getRubyRepositoryStringWithVersion() )
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

	static String getRubyRepositoryString(){
		return 'registry.access.redhat.com/rhscl/ruby-22-rhel7'
	}

	static String  getRubyRepositoryStringWithVersion(){
		return 'registry.access.redhat.com/rhscl/ruby-22-rhel7:latest'
	}
	
	static String getPushRepo(){
		return getRubyRepositoryStringWithVersion()
	}
	static String getRepositoryGoodString(){
		return dockerClient.buildRepositoryString( DOCKER_REGISTRY_HOST, PROJECT_NAME, IMAGE_NAME )
	}

	static String getRepositoryGoodStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( DOCKER_REGISTRY_HOST, PROJECT_NAME, IMAGE_NAME, 'latest' )
	}

	static String getRepositoryBadString(){
		return dockerClient.buildRepositoryString( DOCKER_REGISTRY_HOST, PROJECT_NAME, 'foobar' )
	}

	static String getRepositoryBadStringWithVersion(){
		return dockerClient.buildRepositoryStringWithVersion( DOCKER_REGISTRY_HOST, PROJECT_NAME, 'foobar', 'latest' )
	}
}
