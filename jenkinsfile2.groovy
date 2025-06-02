pipeline{
    agent {label 'sonar-03'}
    options{
        buildDiscarder(logRotator(numToKeepStr: '2'))
    }
    stages{
        stage('checkout'){
             steps{
                checkout scmGit(branches: [[name: "*/Dev"]], 
                extensions: [], 
                userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])

                sh 'pwd && ls -ltr'
             }
        }
       stage('sonar analysis'){
        steps{
            withSonarQubeEnv('MySonar'){
                sh 'sonar-scanner'
            }
        } 
       }
       stage('QualityGate'){
        steps{
            timeout(time: 2, units:'MINUTES'){
                def qg = waitForQualityGate()
                if(qg.status != 'OK'){
                    error "Pipeline aborted due to quality  gate failure: ${qg.status}"
                }
            }
        
       }
}
}

