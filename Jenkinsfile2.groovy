pipeline{
    agent {label 'sonar-03'}
    stages{
        stage('checkout'){
             steps{
                checkout scmGit(branches: [[name: "*/Dev"]], 
                extensions: [], 
                userRemoteConfigs: [[url: 'https://github.com/Sk93804/Maven-tomcat.git']])
             }
        }
        stage('Sonar Analysis'){
            steps{
                withSonarQubeEnv('MySonar') {
                    sh 'sonar-scanner'
                }
            }
        }
        stage('QualityGateCheck'){
            steps{
                timeout(time:2, unit: 'MINUTES')
                waitForQualityGate abortPipeline: true
            }
            post{
                success{
                    gh pr create --title "Automated PR from Jenkins after Quality Gate Passed" \
                   --body "This PR was raised automatically after SonarQube Quality Gate passed." \
                   --base main \
                   --head Dev
                }
                failure{
                    echo "The pipeline has failed to pass the Quality gate"
                }
            }
        }
    }
}
