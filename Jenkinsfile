pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials') // Docker Hub creds stored in Jenkins
        IMAGE_TAG = "${env.BUILD_NUMBER}" // Build number as image tag
    }
    stages {
        stage('Setup Tools') {
            steps {
                sh '''
                # Update package list
                apt-get update
                
                # Install Maven
                apt-get install -y maven
                
                # Install Docker
                apt-get install -y docker.io
                usermod -aG docker jenkins  # Ensure jenkins user can run docker
                
                # Install Helm
                curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
                chmod +x get_helm.sh && ./get_helm.sh
                
                # Install Trivy
                apt-get install -y wget
                wget https://github.com/aquasecurity/trivy/releases/download/v0.51.1/trivy_0.51.1_Linux-64bit.deb
                dpkg -i trivy_0.51.1_Linux-64bit.deb
                
                # Verify installations
                mvn --version
                docker --version
                helm version
                trivy --version
                '''
            }
        }
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
                sh '''
                # Configure Docker to use Minikube's daemon (for local builds)
                eval $(minikube -p minikube docker-env)
                
                # Deploy with Helm
                helm upgrade --install microservices-app ./helm \
                    --set inventory-service.image.tag=${IMAGE_TAG} \
                    --set order-service.image.tag=${IMAGE_TAG} \
                    --namespace my-app --create-namespace
                '''
            }
        }
    }
    post {
        always {
            jacoco(
                execPattern: '**/target/jacoco.exec',
                classPattern: '**/target/classes',
                sourcePattern: '**/src/main/java',
                minimumLineCoverage: '0.80'
            )
        }
        success {
            echo 'Pipeline completed successfully! Services deployed to Minikube.'
        }
        failure {
            sh 'helm rollback microservices-app --namespace my-app || true' // Ignore failure if no release exists
            echo 'Pipeline failed. Attempted rollback.'
        }
    }
}
