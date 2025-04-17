pipeline {
    agent any
    parameters{string(name: 'Branch', defaultValue: 'main', description: 'Branch to checkout')}
    environment {
        branch = '*/params.Branch'
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '2'))
        timestamps()
        skipDefaultCheckout()
        timeout(time: 10, unit:'SECONDS')
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

