pipeline {
    agent none
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
        stage('Integration-Test'){
            agent{ label 'slave-01' }
            steps{
                sh 'mvn integration-test'
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
