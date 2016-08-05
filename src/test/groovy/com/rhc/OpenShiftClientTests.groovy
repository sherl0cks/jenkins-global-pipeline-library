package com.rhc

import org.junit.BeforeClass
import org.junit.Test

class OpenShiftClientTests{
	
	def static hostName = "env3-1-master.innovation.labs.redhat.com"
	def static appName = "infographic-node-app"
	def static projectName = "holmes-playground"
	def static openShiftClient = new com.rhc.OpenShiftClient()
	
	@BeforeClass
	static void shouldLoginToOpenShift(){
		openShiftClient.login( hostName )
		def CommandOutput output = openShiftClient.getWhoAmI()
		assert output.standardOut.trim().equals( 'joe' )
	}
	
	@Test
	void shouldStartBuild(){
		def CommandOutput buildName = openShiftClient.startBuild( appName, projectName )
		def pattern = ~/${appName}-[1-9]+/
		assert buildName.standardOut =~ pattern
	}
	
	@Test
	void shouldGetUserToken(){
		def CommandOutput userToken = openShiftClient.getUserToken()
		def pattern = ~/[A-Za-z0-9-_]+/
		assert userToken.standardOut.trim() =~ pattern
	}
}