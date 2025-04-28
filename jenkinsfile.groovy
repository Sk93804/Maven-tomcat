pipeline{
    agent none
    stages{
        stage('Cleaning WS'){
            agent{ label 'slave-01'}
            steps{
                cleanWs()
            }
        }
    }
}