pipeline {
    environment {
        branch = '*/main'
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    
    agent none  // To specify no global agent for the pipeline, and define agents within individual stages.

    stages {
        stage("SCM") {
            agent { label 'slave-01' }  // Agent specific to this stage
            steps {
                checkout scmGit(
                    branches: [[name: "${env.branch}"]],
                    extensions: [],
                    userRemoteConfigs: [[url: "${env.url}"]]
                )
            }
        }

        stage("Listing") {
            steps {
                sh 'ls -lrt'
            }
        }
    }
}

