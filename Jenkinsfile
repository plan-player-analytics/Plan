pipeline {
    agent any

    stages {
        dir("Plan") {
            stage('Test') {
                steps {
                    script {
                        sh './gradlew clean test --no-daemon' //run a gradle task
                    }
                }
            }
        }
    }
}
