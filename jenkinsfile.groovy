pipeline {
    agent any
    environment {
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    parameters {
        choice(name: 'Branch', choices: ['main', 'feature', 'Dev'], description: 'Select the branch to checkout the code')
        booleanParam(name: 'RUN_STAGE', defaultValue: true, description: 'Do you want to list files?')
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }
    stages {
        stage("Checkout") {
            agent { label 'slave-01' }
            steps {
                script {
                    if (params.Branch == 'main') {
                        checkout scmGit(
                            branches: [[name: "*/${params.Branch}"]],
                            extensions: [],
                            userRemoteConfigs: [[url: "${env.url}"]]
                        )
                    } else {
                        echo "Skipping checkout because Branch = ${params.Branch}"
                    }
                }
            }
        }

        stage("Listing") {
            agent { label 'slave-01' }
            steps {
                script {
                    if (params.RUN_STAGE) {
                        sh 'ls -lrt'
                    } else {
                        echo "Listing stage skipped as RUN_STAGE is false"
                    }
                }
            }
        }
    }
}
