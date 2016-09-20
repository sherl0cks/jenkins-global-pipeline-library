package com.rhc

class CommandExecutor implements Serializable {

	CommandOutput executeInJenkinsOrGroovy( String command ){

		try {
			sh "${command} > output.txt"
			String outputString = readFile( 'output.txt' )
			CommandOutput output = new CommandOutput()
			output.standardOut = outputString
			return output
		} catch (err){
			if ( err instanceof MissingMethodException ) {
				// this means we are running outside of Jenkins
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

	int getCommandTimeout(){
		return 3000000;
	}

}