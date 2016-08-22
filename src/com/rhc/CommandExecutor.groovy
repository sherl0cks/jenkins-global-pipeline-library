package com.rhc

import javax.management.InstanceOfQueryExp;


def CommandOutput executeInJenkinsOrGroovy( String command ){

	try {
		sh "${command} > output.txt"
		String outputString = readFile( 'output.txt' )
		CommandOutput output = new CommandOutput()
		output.standardOut = outputString
		return output
	} catch (err){
		if ( err instanceof MissingMethodException ) { // this means we are running outside of Jenkins
			println( "Jenkins pipeline syntax failed. Falling back to groovy.execute() ")
			StringBuilder sout = new StringBuilder()
			StringBuilder serr = new StringBuilder()
			try {
				Process proc = command.execute()
				proc.consumeProcessOutput(sout, serr)
				proc.waitForOrKill( getCommandTimeout() )
				println("Provided command ${command} suceeded with the following output:")
				println "out:$sout\nerr:$serr"
			} catch ( err2 ){
				println("Provided command ${command} failed with the following exception:")
				println( err2 )
			} finally {
				CommandOutput output = new CommandOutput()
				output.standardOut = sout
				output.standardErr = serr
				return output;
			}
		} else { // this means the command failed in Jenkins
			throw err
		}
	}
}

// This currently cannot be tested outside of Jenkins runtime
// we need to add support for the dir DSL or import Jenkins runtime to our unit tests
def CommandOutput buildApplication( config ){
    if ( config.appRootContext ){
        dir ( config.appRootContext ){ 
            return executeBuildCommands(config)
        }
    } else {
        return executeBuildCommands(config)
    }
}

def String getToolHome( String toolName ){
	return tool( toolName )
}


def getCommandTimeout(){
	return 600000;
}