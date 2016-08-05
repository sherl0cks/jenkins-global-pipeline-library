package com.rhc

import org.junit.Test

class CommandExecutorTests {

	def commandExecutor = new CommandExecutor();
	
	@Test
	void shouldPrintToStandardOut(){
		def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( 'pwd' )
		assert !output.standardOut.empty
		assert output.standardErr.empty
	}
	
	@Test
	void shouldPrintToStandardError(){
		def CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( 'thisCommandShouldFail' )
		assert output.standardOut.empty
		assert output.standardErr.empty
	}
}
