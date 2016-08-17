package com.rhc

import org.junit.Test

class CommandExecutorTests {

	CommandExecutor commandExecutor = new CommandExecutor();
	
	@Test
	void shouldPrintToStandardOut(){
		CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( 'pwd' )
		assert !output.standardOut.empty
		assert output.standardErr.empty
	}
	
	@Test
	void shouldPrintToStandardError(){
		CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( 'thisCommandShouldFail' )
		assert output.standardOut.empty
		assert output.standardErr.empty
	}
}
