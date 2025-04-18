pipeline {
    agent any
    parameters{string(name: 'Branch', defaultValue: 'main', description: '')}
    environment {
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
            when {
                expression {params.Branch == 'main'}
            }
            steps {
                checkout scmGit(
                    branches: [[name: "*/${params.Branch}"]],
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

