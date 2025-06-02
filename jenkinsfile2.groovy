pipeline{
    agent { label 'sonar-03'}
    stages{
        stage('chekout'){

        }
        stage('sonar-scan'){
            withSonarQubeEnv('MySonar'){
                sh 'sonar-scanner'
            }
        }
        stage('Qualitygate'){
            steps{
                timeout(time: 2, unit:'MINUTES'){
                waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}