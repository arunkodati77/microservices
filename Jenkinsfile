pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials') // Store in Jenkins
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/arunkodati77/microservices.git'
            }
        }
        stage('Build & Test') {
            parallel {
                stage('Inventory Service') {
                    steps {
                        dir('inventory-service') {
                            sh 'mvn clean package'
                            sh 'mvn test jacoco:report'
                            sh 'docker build -t aruncoolprojects/inventory-service:${IMAGE_TAG} .'
                        }
                    }
                }
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh 'mvn clean package'
                            sh 'mvn test jacoco:report'
                            sh 'docker build -t aruncoolprojects/order-service:${IMAGE_TAG} .'
                        }
                    }
                }
            }
        }
        stage('Security Scan') {
            steps {
                sh 'trivy image --exit-code 1 --severity HIGH,CRITICAL aruncoolprojects/inventory-service:${IMAGE_TAG}'
                sh 'trivy image --exit-code 1 --severity HIGH,CRITICAL aruncoolprojects/order-service:${IMAGE_TAG}'
            }
        }
        stage('Push Images') {
            steps {
                sh 'echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin'
                sh 'docker push aruncoolprojects/inventory-service:${IMAGE_TAG}'
                sh 'docker push aruncoolprojects/order-service:${IMAGE_TAG}'
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sh 'helm upgrade --install microservices-app ./helm --set inventory-service.image.tag=${IMAGE_TAG} --set order-service.image.tag=${IMAGE_TAG} --namespace my-app --create-namespace'
            }
        }
    }
    post {
        always {
            jacoco execPattern: '**/target/jacoco.exec', minimumCoverage: '0.80'
        }
        failure {
            sh 'helm rollback microservices-app --namespace my-app'
        }
    }
}
