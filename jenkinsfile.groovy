pipeline {
    agent any
    environment {
        branch = '*/main'
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '2'))
        timestamps()
        skipDefaultCheckout()
        timestampFormat('dd-MM-yyyy HH:mm:ss')
    }
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

