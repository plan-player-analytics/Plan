pipeline {
    agent any

    stages {
        stage('Clone') {
            steps {
                checkout scm   
            }
        }
        stage('Test') {
            steps {
                script {
                    sh 'pwd'
                    sh 'ls'
                    sh 'cd Plan'
                    sh './gradlew clean test --no-daemon' //run a gradle task
                }
            }
        }
    }
}
