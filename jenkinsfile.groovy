pipeline {
    agent any
    environment {
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    tools{
        maven 'Maven3.8.6'
    }
    parameters {
        choice(name: 'Branch', choices: ['main', 'feature', 'Dev'], description: 'Select the branch to checkout the code')
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
                        sh 'mvn -v'
                    } else {
                        echo "Skipping checkout because Branch = ${params.Branch}"
                    }
                }
            }
        }

        stage("Build") {
            agent { label 'slave-01' }
            steps {
                sh 'cd /home/ubuntu/jenkins/workspace/First_job/'
                sh 'mvn clean package'
        }
    }
}
