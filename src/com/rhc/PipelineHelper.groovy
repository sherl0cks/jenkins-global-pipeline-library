package com.rhc

import javax.activation.CommandObject;

class PipelineHelper{
	OpenShiftClient oc = new OpenShiftClient()
	DockerClient dockerClient = new DockerClient()
	CommandExecutor commandExecutor = new CommandExecutor()
	
	CommandOutput buildApplication( PipelineConfig config ){
		CommandOutput output = commandExecutor.buildApplication( config )
	}

	List<CommandOutput> buildAndDeployImage( PipelineConfig config ){
		List<CommandOutput> outputs
		if ( config.buildImageCommands == null ){
			println 'No buildImageCommands, using default OpenShift image build and deploy'
			CommandOutput result = oc.startBuildAndWaitUntilComplete( config.appName, config.projectName )
			outputs = new ArrayList<CommandOutput>()
			outputs.push( result )
		} else {
			println 'Found buildImageCommands, executing in shell'
			List<CommandOutput> results = executeListOfShellCommands( config.buildImageCommands )
			outputs = results
		}
		return outputs
	}

	String[] getBuildTools(){
		return ['node-0.10', 'Maven3.3.9'];
	}

	List<CommandOutput> executeBuildCommands( PipelineConfig config ){
		List<CommandOutput> outputs 
		String[] tools = getBuildTools()
		
		if ( config.buildTool == null ){
			println 'No build tool declared. Any commands will execute directly in the shell.'
			outputs = executeListOfShellCommands( config.buildCommands )
		} else if ( config.buildTool in tools ) {
			println "Using build tool: ${config.buildTool}"
			outputs = executeListOfToolCommands( config.buildCommands, config.buildTool )
		} else {
			throw new RuntimeException( "${config.buildTool} is currently unsupported. Please select one of ${tools}." )
		}
		return outputs
	}

	List<CommandOutput> executeListOfShellCommands( String[] commandList ){
		List<CommandOutput> outputs = new ArrayList<CommandOutput>()
		for (int i=0; i<commandList.length; i++){
			String command = commandList[i]
			CommandOutput output = commandExecutor.executeInJenkinsOrGroovy( command )
			outputs.push( output )
		}
		return outputs
	}

	void executeListOfToolCommands( String[] commandList, String buildTool ){
		String toolHome = commandExecutor.getToolHome( buildTool )
		for (int i=0; i<commandList.length; i++){
			def command = commandList[i]
			commandExecutor.executeInJenkinsOrGroovy( "${toolHome}/bin/${command}" )
		}
	}

	CommandOutput promoteImage( String currentNamespace, String newNamespace, PipelineConfig config ){
		String currentImageRepositoryWithVersion = dockerClient.buildRepositoryStringWithVersion( config.dockerRegistry, currentNamespace, config.appName, 'latest' )
		String newImageRepositoryWithVersion = dockerClient.buildRepositoryStringWithVersion( config.dockerRegistry, newNamespace, config.appName, 'latest' )

		return dockerClient.promoteImageBetweenRepositories( currentImageRepositoryWithVersion, newImageRepositoryWithVersion )
	}

	void login( PipelineConfig config ){
		oc.login( config.ocHost )
		dockerClient.login( config.dockerRegistry, oc.getTrimmedUserToken() )
	}
}