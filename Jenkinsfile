// Build pipeline for ChatFeelings.
//
// The container image supplies the JDK and ./mvnw supplies Maven, so the build
// no longer depends on a Maven being installed -- and on PATH -- on whichever
// node Jenkins happens to pick. The Maven version is pinned in
// .mvn/wrapper/maven-wrapper.properties.
//
// Needs the Docker Pipeline plugin and a reachable Docker daemon on the node.

pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21'
        }
    }

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
