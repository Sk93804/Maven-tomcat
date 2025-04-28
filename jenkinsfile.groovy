pipeline{
    agent none
    stages{
        stage('Cleaning WS'){
            steps{
                cleanWs()
            }
        }
    }
}