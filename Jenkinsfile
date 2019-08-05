pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                dir("Plan") {
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
        stage('Checkstyle') {
            steps {
                dir("Plan") {
                    script {
                        sh './gradlew checkstyleMain checkstyleTest'
                    }
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                dir("Plan") {
                    script {
                        withSonarQubeEnv() {
                            sh './gradlew sonarqube -Dsonar.organization=player-analytics-plan'
                        }
                    }
                }
            }
        }
    }
}
