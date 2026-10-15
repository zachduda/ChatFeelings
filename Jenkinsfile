// Build pipeline for ChatFeelings.
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        MAVEN_USER_HOME = "${WORKSPACE}/.m2"
    }

    stages {
        stage('Environment') {
            steps {
                sh '''
                    echo "node:      ${NODE_NAME:-unknown}"
                    echo "workspace: ${WORKSPACE}"
                    echo "PATH:      ${PATH}"
                    java -version
                    ./mvnw -v
                '''
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B -ntp -Dmaven.repo.local="$MAVEN_USER_HOME/repository" package'
            }
        }
    }

    post {
        success {
            // The shade plugin leaves the pre-shaded jar behind as original-*.jar.
            archiveArtifacts artifacts: 'target/*.jar',
                             excludes: 'target/original-*.jar',
                             fingerprint: true
        }
    }
}
