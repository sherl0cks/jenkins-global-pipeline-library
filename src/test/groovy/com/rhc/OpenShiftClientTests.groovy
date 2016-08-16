package com.rhc

import java.util.regex.Pattern

import org.junit.BeforeClass
import org.junit.Test

import groovy.json.JsonSlurper;

class OpenShiftClientTests{
	
	static String hostName = "env3-1-master.innovation.labs.redhat.com"
	static String appName = "infographic-node-app"
	static String projectName = "holmes-playground"
	static OpenShiftClient openShiftClient = new com.rhc.OpenShiftClient()
	
	@BeforeClass
	static void shouldLoginToOpenShift(){
		openShiftClient.login( hostName )
		CommandOutput output = openShiftClient.getWhoAmI()
		assert output.standardOut.trim().equals( 'joe' )
	}
	
	@Test
	void shouldStartBuild(){
		CommandOutput buildName = openShiftClient.startBuild( appName, projectName )
		Pattern pattern = ~/${appName}-[1-9]+/
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
		String buildName = openShiftClient.startBuildAndWaitUntilComplete( appName, projectName )
		String status = openShiftClient.getBuildStatus( buildName, projectName )
		assert status.equalsIgnoreCase( 'Complete' )
	}
	
}