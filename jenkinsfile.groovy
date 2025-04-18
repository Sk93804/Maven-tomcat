pipeline {
    agent any
    parameters{choice(name: 'Branch', choices: ['DEV', 'PROD', 'QA'], description: '')}
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
                anyOf{
                    expression{ params.Branch == 'DEV'}
                    expression{ params.Branch == 'QA' }
                    expression{ params.Branch == 'PROD' }
                }
            }
            steps {
                checkout scmGit(
                    branches: [[name: "*/main"]],
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

