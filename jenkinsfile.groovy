pipeline{
    environment{
        branch = '*/main'
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    stages{
        agent{ label 'slave-01'}
        stage("SCM"){
            steps{
            checkout scmGit(branches: [[name: '${env.branch}']], 
            extensions: [], 
            userRemoteConfigs: [[url: '${env.url}']])
            }
        }
        stage("Listing"){
            steps{
                sh 'ls -lrt'
            }
        }

    }
}