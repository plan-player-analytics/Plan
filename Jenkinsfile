pipeline {
    agent any

node {
    stages {
        stage('Clone') {
            steps {
                checkout scm
                sh 'cd Plan'
                sh 'pwd'           
            }
        }
        stage('Test') {
            steps {
                script {
                    try {
                        sh './gradlew clean test --no-daemon' //run a gradle task
                    } finally {
                        junit '**/build/test-results/test/*.xml' //make the junit test results available in any case (success & failure)
                    }
                }
            }
        }
    }
}

}
