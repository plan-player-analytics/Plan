pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir("Plan") {
                    script {
                        sh 'rm -rf builds'
                        sh './gradlew clean shadowJar --parallel'
                    }
                }
                archiveArtifacts artifacts: 'Plan/builds/*.jar', fingerprint: false
            }
        }
        stage('Tests') {
            steps {
                dir("Plan") {
                    script {
                        try {
                            sh './gradlew test --parallel'
                        } finally {
                            junit '**/build/test-results/test/*.xml'
                        }
                    }
                }
            }
        }
        stage('Checkstyle') {
            steps {
                dir("Plan") {
                    script {
                        sh './gradlew checkstyleMain checkstyleTest --parallel'
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
    post {
        always {
            dir("Plan") {
                script {
                    sh './gradlew clean --parallel'
                    sh 'rm -rf builds'
                }
            }
        }
    }
}
