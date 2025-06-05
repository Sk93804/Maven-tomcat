pipeline{
    agent {label 'Owasp'}
    environment{
        NVD_API = "80014e96-9700-426b-af09-d7e5b2f6ac7e"
    }
    stages{
        stage('Checkout'){
            steps{
                checkout scmGit(branches: [[name: '*/main']], 
                extensions: [], 
                userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])
            }
        }
        stage('Build'){
            steps{
                script{
                        sh 'mvn clean package'
                }
            }
        }
        stage('Owasp-scan'){
            steps{
                script{
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
                }
            }
            post{
                always{
                    publishHTML (target : [
                        reportName: "Dependency-check-report"
                        reportDir: './dependency-check-report/'
                        reportFiles: "dependency-check-report.html"
                        reportTitles: 'DC-check'
                    ])
                }
            }
        }
    }
}