@Library('jenkins-shared-lib') _

pipeline {
    agent any

    stages {
        stage('Dummy Task') {
            steps {
                echo "Simulating build..."
            }
        }
    }

    post {
        always {
            sendNotification(env)
        }
    }
}
