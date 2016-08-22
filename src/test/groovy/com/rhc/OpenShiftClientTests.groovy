package com.rhc

import java.util.regex.Pattern

import org.junit.BeforeClass
import org.junit.Test

import groovy.json.JsonSlurper;
import groovy.transform.TypeChecked

@TypeChecked
class OpenShiftClientTests{

	static final String HOST_NAME = "env3-1-master.innovation.labs.redhat.com"
	static final String APP_NAME = "infographic-node-app"
	static final String PROJECT_NAME = "holmes-playground"
	static final OpenShiftClient openShiftClient = new com.rhc.OpenShiftClient()


	@BeforeClass
	static void shouldLoginToOpenShift(){
		openShiftClient.login( HOST_NAME )
		CommandOutput output = openShiftClient.getWhoAmI()
		assert output.standardOut.trim().equals( 'joe' )
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
		String buildName = openShiftClient.startBuildAndWaitUntilComplete( APP_NAME, PROJECT_NAME ).standardOut.trim()
		String status = openShiftClient.getBuildStatus( buildName, PROJECT_NAME )
		assert status.equalsIgnoreCase( 'Complete' )
	}
}