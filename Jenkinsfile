pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir("Plan") {
                    script {
                        sh 'rm -rf builds'
						sh 'sed -i -e s/%buildNumber%/\$BUILD_NUMBER/g build.gradle'
						sh 'sed -i -e s/%buildNumber%/\$BUILD_NUMBER/g common/src/main/resources/plugin.yml'
						sh 'sed -i -e s/%buildNumber%/\$BUILD_NUMBER/g common/src/main/resources/bungee.yml'
						sh 'sed -i -e s/%buildNumber%/\$BUILD_NUMBER/g sponge/src/main/java/com/djrapitops/plan/PlanSponge.java'
						sh 'sed -i -e s/%buildNumber%/\$BUILD_NUMBER/g velocity/src/main/java/com/djrapitops/plan/PlanVelocity.java'
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
