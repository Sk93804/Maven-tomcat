pipeline {
    agent any
    environment {
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    parameters {
        choice(name: 'Branch', choices: ['main', 'feature', 'Dev'], description: 'Select the branch to checkout the code')
    }
    options {
        skipDefaultCheckout()
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
            parameters{ booleanParam(name: 'RUN_STAGE?', defaultValue: true, description: '')}
            agent { label 'slave-01' }
            steps {
                script{
                    if (${params.RUN_STAGE} == "true"){
                     sh 'ls -lrt'
                    }
                }
            }
        }
    }
}
