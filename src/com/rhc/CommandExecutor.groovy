package com.rhc

import javax.management.InstanceOfQueryExp;


def CommandOutput executeInJenkinsOrGroovy( String command ){

	try {
		sh "${command} > output.txt"
		def outputString = readFile( 'output.txt' )
		def CommandOutput output = new CommandOutput()
		output.standardOut = outputString
		return output
	} catch (err){
		if ( err instanceof MissingMethodException ) { // this means we are running outside of Jenkins
			println( "Jenkins pipeline syntax failed. Falling back to groovy.execute() ")
			def sout = new StringBuilder(), serr = new StringBuilder()
			try {
				def proc = command.execute()
				proc.consumeProcessOutput(sout, serr)
				proc.waitForOrKill( getCommandTimeout() )
				println("Provided command ${command} suceeded with the following output:")
				println "out:$sout\nerr:$serr"
			} catch ( err2 ){
				println("Provided command ${command} failed with the following exception:")
				println( err2 )
			} finally {
				def CommandOutput output = new CommandOutput()
				output.standardOut = sout
				output.standardErr = serr
				return output;
			}
		} else { // this means the command failed in Jenkins
			throw err
		}
	}
}

//def Comma

def getCommandTimeout(){
	return 600000;
}