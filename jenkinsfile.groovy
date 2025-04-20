pipeline{
    agent none
    stages{
        stage('Checkout'){
            agent{ label 'slave-01'}
            steps{
                checkout scmGit(branches: [[name: '*/main']],
                 extensions: [], 
                 userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])
            }
        }
        stage('Build'){
            agent{ label 'slave-01'}
            steps{
               sh 'mvn clean package'
               sh 'ls -ltr'
               sh cd './target'
            }
        }
    }
}