pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        skipDefaultCheckout(true)
    }

    stages {
        stage('docker sonar'){
            agent{ label 'sonar-03'}
            steps{
                // sh 'docker rm  sonarQube'
                sh 'docker run -d --name sonarQube  -p 9000:9000 -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -v sonarqube_data:/opt/sonarqube/data  -v sonarqube_logs:/opt/sonarqube/logs sonarqube:latest'
            }
        }
        stage('Checkout') {
            agent { label 'slave-01' }
            steps {
                checkout scmGit(branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )
            }
        }

        stage('Build and Integration Tests') {
            parallel {
                stage('Build') {
                    agent { label 'slave-01' }
                    steps {
                        sh 'mvn test'
                        sh 'ls -ltr'
                    }
                }
                stage('Integration-Test') {
                    agent { label 'slave-01' }
                    steps {
                        sh 'mvn integration-test'
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
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=helloworld -Dsonar.host.url=http://52.66.100.152:9000'
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
                sh 'mvn clean package'
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
