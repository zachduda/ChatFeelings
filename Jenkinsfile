// Build pipeline for ChatFeelings.
//
// ./mvnw supplies Maven itself, so the build only needs a JDK on the node --
// no system Maven, and no Docker Pipeline plugin / Docker daemon required.
// The Maven version is pinned in .mvn/wrapper/maven-wrapper.properties.
// The node running this must have a JDK on PATH matching the project's
// target release (21).

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        // Jenkins starts the container under the agent's own uid, so $HOME inside
        // it is not writable. Point the wrapper -- and below, the dependency
        // cache -- at the workspace instead, which is bind-mounted and owned by
        // that uid. It also survives between builds, so it doubles as the cache.
        MAVEN_USER_HOME = "${WORKSPACE}/.m2"
    }

    stages {
        // Cheap, and it means a future environment problem reports itself in the
        // log instead of surfacing as a bare "mvn: not found".
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
