pipeline {
    agent any

    environment {
        APP_NAME = 'deploy-lab'
        NAMESPACE = 'deploy-lab'
        IMAGE_TAG = "${BUILD_NUMBER}"
        IMAGE_NAME = "deploy-lab:${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and test') {
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('Build Docker image') {
            steps {
                sh 'docker build -t ${IMAGE_NAME} .'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -f k8s/service.yaml
                    kubectl apply -f k8s/deployment.yaml
                    kubectl -n ${NAMESPACE} set image deployment/${APP_NAME} ${APP_NAME}=${IMAGE_NAME}
                    kubectl -n ${NAMESPACE} set env deployment/${APP_NAME} APP_VERSION=${IMAGE_TAG}
                    kubectl -n ${NAMESPACE} rollout status deployment/${APP_NAME} --timeout=120s
                '''
            }
        }

        stage('Smoke test') {
            steps {
                sh '''
                    kubectl -n ${NAMESPACE} port-forward service/${APP_NAME} 18080:8080 > /tmp/deploy-lab-port-forward.log 2>&1 &
                    PF_PID=$!
                    sleep 5
                    curl --fail http://127.0.0.1:18080/actuator/health
                    curl --fail http://127.0.0.1:18080/api/hello
                    kill $PF_PID || true
                '''
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        success {
            echo "Deployment ${BUILD_NUMBER} zakończony sukcesem"
        }
        failure {
            echo 'Pipeline zakończył się błędem. Sprawdź logi konkretnego stage.'
        }
    }
}
