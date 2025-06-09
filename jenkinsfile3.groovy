pipeline {
    agent none
    options{
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }
    environment{
        IMAGE_NAME= "Hellowrld"
        IMAGE_TAG= "latest"
        TEMPLATE_PATH = "/home/ubuntu/html.tpl"
        REGISTRY_URL = "https://index.docker.io/v1/"
        REGISTRY_CRED = 'DockerCred'
    }
    stages {
        stage('clean ws'){
            agent { label 'Owasp' }
            steps{
                cleanWs()
            }
        }
        stage('Checkout') {
            agent { label 'Owasp' }
            steps {
                checkout scmGit(
                    branches: [[name: '*/main']], 
                    extensions: [], 
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']]
                )
            }
        }

        stage('Build') {
            agent { label 'Owasp' }
            steps {
                script {
                    sh 'mvn clean package'
                    sh 'ls -lrt'
                    stash includes: 'target/helloworld.war', name: 'warfile'
                    stash includes: 'Dockerfile.dockerfile', name: 'Dockerfile'

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
            agent { label 'Owasp' }
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
            agent { label 'Docker'}
            steps{
                script{
                unstash 'warfile'
                unstash 'Dockerfile'
                sh 'ls -lrt'
                def customImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}", '--build-arg PKG=APT -f Dockerfile.dockerfile .')
                sh 'docker images'
                
                }
            }
        }
        stage('Trivy scan and upload the image'){
            steps{
               script{
                  sh "trivy image -f template --template ${TEMPLATE_PATH} --output trivy-report.html ${IMAG_NAME}:${IMAGE_TAG}"
               }
            }
            post{
              success{
                script{
                      docker.withRegistry("${REGISTRY_URL}", "${REGISTRY_CRED}")
                      customImage.push()
                    }
                
                    publishHTML (target: [
                        reportDir: '.',
                        reportFiles: "trivy-report.html",
                        reportName: "${IMAGE_NAME}:${TAG_NAME} Trivy scan report",
                        reportTitle: 'Trivy Scan'
                    ])
              }
            }
        }
    }
}
