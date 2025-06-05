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
                    dir('Maven-tomcat'){
                        sh 'mvn clean package'
                    }
                }
            }
        }
        stage('Owasp-scan'){
            steps{
                script{
                    dependencyCheck additionalArguments:"""
                    --project "HELLOWORLD"
                    -o ./ \
                    -s ./ \
                    -f "ALL"
                    --noupdate
                    --data /home/ubuntu/dc-data
                    --nvdApiKey $NVD_API
                    """
                    dependencyCheckPublisher pattern: 'dependency-check-report.html'
                }
            }
        }
    }
}