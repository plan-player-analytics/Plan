pipeline {
    agent any

    stages {
            stage('Test') {
                steps {
                            dir("Plan") {
                    script {
                        sh './gradlew clean test --no-daemon' //run a gradle task
                    }
                }
            }
        }
    }
}
