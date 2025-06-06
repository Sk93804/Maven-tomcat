pipeline {
    agent { label 'Owasp' }

    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/main']], 
                    extensions: [], 
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'mvn clean package'
                    stash includes: 'target/helloworld.war', name: 'app-war'
                }
            }
        }
        // stage('Deploy Artifacts'){
        //     steps{
        //         withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'USER', passwordVariable: 'PASS')]){
        //             sh '''
        //             curl -v  -u $USER:$PASS --upload-file target/helloworld.war \
        //             http://13.233.173.87:8081/repository/maven-raw/helloworld.war

        //             '''
        //         }
        //     }
        // }

        stage('Owasp-scan') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'NVD_API', variable: 'NVD_API')]) {
                        sh '''
                        /opt/dependency-check/bin/dependency-check.sh \
                        --project "HELLOWORLD" \
                        -o ./dependency-check-report \
                        -s ./ \
                        -f ALL \
                        --nvdApiKey $NVD_API \
                        --data /home/ubuntu/dc-data \
                        --noupdate
                        '''
                        sh "echo \"NVD_API = $NVD_API\""
                    }
                }
            }
            post {
                always {
                    publishHTML(target: [
                        reportName: "Dependency-check-report",
                        reportDir: './dependency-check-report/',
                        reportFiles: "dependency-check-report.html",
                        reportTitles: 'DC-check'
                    ])
                }
            }
        }
        stage('Docker image'){
            agent { label 'sonar-03'}
            steps{
                unstash 'app-war'
                sh 'docker build -t helloworld:lts .'
            }
        }
    }
}
