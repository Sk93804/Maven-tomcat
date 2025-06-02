pipeline {
    agent { label 'sonar-03' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        skipDefaultCheckout()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: "*/Dev"]],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )

                sh 'pwd && ls -ltr'
            }
        }

        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('MySonar') {
                    sh "${tool 'SonarScanner'}/bin/sonar-scanner"
                }
            }
        }

        stage('QualityGate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    script{
                    def qg = waitForQualityGate()
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to Quality Gate failure: ${qg.status}"
                    }
                    }
                }
            }
        }
    }
}
