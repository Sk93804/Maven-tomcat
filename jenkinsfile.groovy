pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }

    stages {
        stage('Checkout') {
            agent { label 'slave-01' }
            steps {
                checkout scmGit(branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )
            }
        }

        stage('Build') {
            agent { label 'slave-01' }
            steps {
                sh 'mvn clean package'
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

        stage('Test Reports') {
            agent { label 'slave-01' }
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('Archive Artifacts') {
            agent { label 'slave-01' }
            steps {
                archiveArtifacts artifacts: 'target/**/*.txt', fingerprint: true
            }
        }
        stage('Start SonarQube'){
            agent { label 'slave-02'}
            steps{
                sh ''' echo "Starting sonarQube Container" 
                docker run -d --name sonarQube  -p 9000:9000 -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -v sonarqube_data:/opt/sonarqube/data  -v sonarqube_logs:/opt/sonarqube/logs sonarqube:latest 
                 echo "Waiting for SonarQube to be ready..."
                 sleep 30

                 docker ps -a     
                '''

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
