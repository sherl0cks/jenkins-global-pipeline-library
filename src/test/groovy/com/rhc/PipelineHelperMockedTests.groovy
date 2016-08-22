package com.rhc

import groovy.mock.interceptor.MockFor

import java.util.List;

import org.junit.Test

class PipelineHelperMockedTests {


	@Test
	void shouldBuildApplication(){
		def mock = new MockFor(CommandExecutor)
		mock.demand.buildApplication{ new CommandOutput( standardOut: 'test value' )  }
		mock.use {
			PipelineHelper helper = new PipelineHelper()
			CommandOutput output = helper.buildApplication(null)
			assert output.standardOut.equals( 'test value' )
			assert output.standardErr == null
		}
		mock.expect.verify()
	}

	@Test
	void shouldBuildAndDeployImageViaOpenShift(){
		def mock = new MockFor(OpenShiftClient)
		mock.demand.startBuildAndWaitUntilComplete{ a, b -> return new CommandOutput( standardOut: a + ' ' + b )  }
		mock.use {
			PipelineHelper helper = new PipelineHelper()
			PipelineConfig config = new PipelineConfig()
			config.appName = 'name'
			config.projectName = 'project'
			List<CommandOutput> outputs = helper.buildAndDeployImage( config )
			assert outputs.size() == 1
			assert outputs.get(0).getStandardOut().equals( 'name project' )
		}
		mock.expect.verify()
	}

	@Test
	void shouldBuildAndDeployImageViaCustomCommands(){
		def mock = new MockFor( CommandExecutor )
		mock.demand.executeInJenkinsOrGroovy { command -> new CommandOutput( standardOut: command) }
		mock.demand.executeInJenkinsOrGroovy { command -> new CommandOutput( standardOut: command) }
		mock.use {
			PipelineHelper helper = new PipelineHelper()
			PipelineConfig config = new PipelineConfig()
			config.buildImageCommands = ['command 1', 'command 2']
			List<CommandOutput> outputs = helper.buildAndDeployImage( config )
			assert outputs.size() == 2
			assert outputs.get(0).getStandardOut().equals( 'command 1' )
			assert outputs.get(1).getStandardOut().equals( 'command 2' )
		}
		mock.expect.verify()
	}

	@Test
	void shouldExecuteBuildCommandsInShell(){
		def mock = new MockFor( CommandExecutor )
		mock.demand.executeInJenkinsOrGroovy { command -> new CommandOutput( standardOut: command) }
		mock.demand.executeInJenkinsOrGroovy { command -> new CommandOutput( standardOut: command) }
		mock.use {
			PipelineHelper helper = new PipelineHelper()
			PipelineConfig config = new PipelineConfig()
			config.buildCommands = [
				'build command 1',
				'build command 2'
			]
			List<CommandOutput> outputs = helper.executeBuildCommands( config )
			assert outputs.size() == 2
			assert outputs.get(0).getStandardOut().equals( 'build command 1' )
			assert outputs.get(1).getStandardOut().equals( 'build command 2' )
		}
		mock.expect.verify()
	}

	// TODO figure out how to test the "tool" function provided by Jenkins. probably just need to import the runtime instead of mocking for that
}
