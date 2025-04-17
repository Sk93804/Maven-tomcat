pipeline{
    agent any
    stages{
        stage("SCM"){
            steps{
            checkout scmGit(branches: [[name: '*/main']], 
            extensions: [], 
            userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])
            }
        }
        stage("Listing"){
            steps{
                sh 'ls -lrt'
            }
        }

    }
}