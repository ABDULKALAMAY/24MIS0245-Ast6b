pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build and Test') {
            steps { bat 'mvn -B clean test' }
        }
        stage('Package') {
            steps { bat 'mvn -B package -DskipTests=false' }
        }
    }

    post {
        always { junit 'target/surefire-reports/*.xml' }
    }
}
