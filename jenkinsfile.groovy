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
    }
    post {
        always {
            emailext(
                to: 'sudheesh.zx@gmail.com',
                subject: "Build Status: $PROJECT_NAME - Build # $BUILD_NUMBER",
                body: """
                  <h3>Build # $BUILD_NUMBER for project $PROJECT_NAME</h3>
                  <p>Status: $BUILD_STATUS</p>
                  <p>Check the console output at <a href="$BUILD_URL">$BUILD_URL</a></p>
                """,
                mimeType: 'text/html',
                attachLog: true
            )
        }
    }
}
