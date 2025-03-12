pipeline {
    agent agent-01
    triggers {
        githubPush()
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
            }
        }
    }
}
