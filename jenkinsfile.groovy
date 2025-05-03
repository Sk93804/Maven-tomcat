@Library('SHARED_LIBRARY') _


pipeline {

    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            agent { label 'slave-01' }
            steps {
                def config = [
                    branch = 'main'
                    url = 'https://github.com/Sk93804/Maven-tomcat.git'
                ]
                script{
                    gitChekout(config)
                }
            }
        }

        stage('Unit test and Integration Tests') {
            parallel {
                stage('Unit-Test') {
                    agent { label 'slave-01' }
                    steps {
                       def command = [ option = 'test']
                       script{
                         Unittest(command)
                       }
                    }
                }
                stage('Integration-Test') {
                    agent { label 'slave-01' }
                    steps {
                        def command = [option = 'integrtaion-test']
                        script{
                               Int-test(command)
                        }
                    }
                }
            }
        }

        stage('Test Reports') {
            agent { label 'slave-01' }
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            agent { label 'slave-01' }
            environment {
                SONARQUBE_ENV = 'MySonar'
            }
            steps {
                script{
                    sonarScan(SONARQUBE_ENV = "${SONARQUBE_ENV}", projectKey = 'helloworld', sonarUrl = 'http://3.109.182.116:9000')
                }
            }
        }

        stage('Quality Gate') {
            agent { label 'slave-01' }
            steps {
                script {
                    sleep(5)
                }
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Package') {
            agent { label 'slave-01' }
            steps {
                script{
                    package(goal = 'clean', option = 'package')
                }
            }
        }

        stage('Archive Artifacts') {
            agent { label 'slave-01' }
            steps {
                archiveArtifacts artifacts: 'target/**/*.txt', fingerprint: true
            }
        }
    }

    post {
        always {
            emailext(
                to: 'sudheesh.zx@gmail.com',
                subject: "Build Status: ${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
                body: """
                    <h3>Build # ${env.BUILD_NUMBER} for project ${env.JOB_NAME}</h3>
                    <p>Status: ${currentBuild.currentResult}</p>
                    <p>Check the console output at <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                """,
                mimeType: 'text/html',
                attachLog: true
            )
        }
    }
}
