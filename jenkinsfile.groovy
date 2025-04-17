Pipeline{
    environment{
        branch = '*/main'
        url = 'https://github.com/Sk93804/Maven-tomcat.git'
    }
    stages{
        
        stage("SCM"){
            agent{ label 'slave-01'}
            steps{
            checkout scmGit(branches: [[name: "${env.branch}"]], 
            extensions: [], 
            userRemoteConfigs: [[url: "${env.url}"]])
            }
        }
        stage("Listing"){
            steps{
                sh 'ls -lrt'
            }
        }

    }
}