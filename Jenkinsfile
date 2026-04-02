pipeline {
    agent any

    environment {
        GEMINI_API_KEY = credentials('GEMINI_API_KEY')
        APP_EMAIL = credentials('APP_EMAIL')
        APP_PASSWORD = credentials('APP_PASSWORD')
        CLIENT_ID = credentials('CLIENT_ID')
        CLIENT_SECRET = credentials('CLIENT_SECRET')
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot backend...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo 'Building React frontend...'
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                sh 'docker-compose build'
            }
        }

        stage('Docker Run') {
            steps {
                echo 'Starting all services...'
                sh 'docker-compose up -d'
            }
        }

    }

    post {
        success {
            echo '✅ JARVIS deployed successfully!'
        }
        failure {
            echo '❌ Build failed! Check logs above.'
        }
    }
}