pipeline {
    agent { label 'Owasp' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        skipDefaultCheckout()
    }

    environment {
        NVD_API_TOKEN = "80014e96-9700-426b-af09-d7e5b2f6ac7e"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: "*/main"]],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )
            }
        }

        stage('Build') {
            steps {
                dir('Maven-tomcat') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('OWASP') {
            steps {
                dependencyCheck additionalArguments: """
                    --project HelloWorld \
                    -o ./ \
                    -s ./Maven-tomcat \
                    -f ALL \
                    --nvdApiKey ${NVD_API_TOKEN}
                """
                dependencyCheckPublisher pattern: 'dependency-check-report.xml'
            }
        }
    }
}
