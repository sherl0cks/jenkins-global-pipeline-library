package com.rhc

import java.util.regex.Pattern

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

/**
 * These tests are designed to run against the Red Hat CDK
 */
class OpenShiftClientTests{
	
	static final String HOST_NAME = "10.1.2.2:8443"
	static final String APP_NAME = "ruby-ex"
	static final String PROJECT_NAME = "sample-project"
	static final OpenShiftClient openShiftClient = new com.rhc.OpenShiftClient()
	static final String USER_NAME = "admin"
	static final String PASSWORD = "admin"
	
	@BeforeClass
	static void shouldLoginToOpenShift(){
		openShiftClient.setWaitIntervalInMilliseconds( 10000 )
		openShiftClient.login( HOST_NAME, USER_NAME, PASSWORD )
		CommandOutput output = openShiftClient.getWhoAmI()
		assert output.standardOut.trim().equals( 'admin' )
		CommandOutput output2 = new CommandExecutor().executeInJenkinsOrGroovy( "oc new-app openshift/ruby:latest~https://github.com/openshift/ruby-ex.git -n ${PROJECT_NAME}" )
		assert output2.standardOut.contains("Success")
		assert output2.standardOut.contains("Build scheduled for \"ruby-ex\"")
	}
	
	@Test
	void shouldStartBuild(){
		CommandOutput buildName = openShiftClient.startBuild( APP_NAME, PROJECT_NAME )
		Pattern pattern = ~/${APP_NAME}-[1-9]+/
		assert buildName.standardOut =~ pattern
	}
	
	@Test
	void shouldGetUserToken(){
		CommandOutput userToken = openShiftClient.getUserToken()
		Pattern pattern = ~/[A-Za-z0-9-_]+/
		assert userToken.standardOut.trim() =~ pattern
	}
	
	@Test
	void shouldStartBuildAndWaitUntilBuildIsComplete(){
		String buildName = openShiftClient.startBuildAndWaitUntilComplete( 'infographic', 'dev-project' )
		//String status = openShiftClient.getBuildStatus( 'infographic', 'dev-project' )
		assert status.equalsIgnoreCase( 'Complete' )
	}
	
	@AfterClass
	static void cleanUpProject(){
		Thread.sleep( 5000 )
		CommandOutput output = new CommandExecutor().executeInJenkinsOrGroovy( 'oc delete all --all -n sample-project' )
	}
	
	
}