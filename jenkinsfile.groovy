pipeline{
    agent none
    stages{
        stage('Code Checkout'){
            agent{ label 'slave-01'}
            steps{
                 checkout scmGit(branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])
            }
        }
        stage('sonar analysis'){
            agent{label 'slave-01'}
            environment{
                SONARQUBE = 'MySonar'
            }
            withSonarQubeEnv("$SONARQUBE"){
                sh "mvn sonar:sonar"
            }
        }
        stage('Quality Gate'){
            steps{
                timeout(time: 2, unit: 'MINUTES')
                waitForQualityGate abortPipeline: true
            }
            input{
                message "The Quality Gate as passed pls merge the PR"
                parameters{
                    choice(name: 'Merge', choices: ['Yes', 'No'])
                }
            }
        }
    }
}