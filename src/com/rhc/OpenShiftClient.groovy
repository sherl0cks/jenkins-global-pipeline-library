package com.rhc;

def startBuild( String appName, String projectName ){
    sh "oc start-build ${appName} -n ${projectName}" //-e ARTIFACT_URL=$hi"
}

def login( String host ){
	sh "oc login ${host} --insecure-skip-tls-verify=true --username=joe --password=redhat" // This clearly shouldn't be hard coded
}