// 需要在jenkins的Credentials设置中配置jenkins-harbor-creds、jenkins-k8s-config参数
pipeline {
    agent any
    environment {
       // HARBOR_CREDS = credentials('jenkins-harbor-creds')
        K8S_CONFIG = credentials('K8S_CONFIG')
        //GIT_TAG = sh(returnStdout: true,script: 'git describe --tags --always').trim()
        GIT_TAG = 'latest'
    }
    parameters {
        string(name: 'HARBOR_HOST', defaultValue: '132.145.91.13:8099', description: 'harbor仓库地址')
        string(name: 'DOCKER_IMAGE', defaultValue: 'chunjin/pipeline-demo', description: 'docker镜像名')
        string(name: 'APP_NAME', defaultValue: 'pipeline-demo', description: 'k8s中标签名')
        string(name: 'K8S_NAMESPACE', defaultValue: 'demo', description: 'k8s的namespace名称')
    }
    stages {
        stage('Maven Build') {
            when { expression { env.GIT_TAG != null } }
            
            steps {
                sh  'mvn -v'
                sh 'mvn clean package -Dfile.encoding=UTF-8 -DskipTests=true'
                stash includes: 'target/*.jar', name: 'app'
            }

        }
        stage('Docker Build') {
            when { 
                allOf {
                    expression { env.GIT_TAG != null }
                }
            }
            agent any
            steps {
                unstash 'app'
               // sh "docker login -u ${HARBOR_CREDS_USR} -p ${HARBOR_CREDS_PSW} ${params.HARBOR_HOST}"
                sh "docker build --build-arg JAR_FILE=`ls target/*.jar |cut -d '/' -f2` -t ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG} ."
                sh "docker push ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG}"
                sh "docker rmi ${params.HARBOR_HOST}/${params.DOCKER_IMAGE}:${GIT_TAG}"
            }
            
        }
        stage('Deploy') {
            when { 
                allOf {
                    expression { env.GIT_TAG != null }
                }
            }
            agent {
                docker {
                    image 'lwolf/helm-kubectl-docker'
                }
            }
            steps {
               
               sh "mkdir -p ~/.kube"
               sh "echo ${K8S_CONFIG} | base64 -d > ~/.kube/config"
               sh "sed -e 's#{IMAGE_URL}#${params.HARBOR_HOST}/${params.DOCKER_IMAGE}#g;s#{IMAGE_TAG}#${GIT_TAG}#g;s#{APP_NAME}#${params.APP_NAME}#g;s#{SPRING_PROFILE}#k8s-test#g' k8s-deployment.tpl > k8s-deployment.yml"
                
                // withKubeConfig([credentialsId: 'k8suser', serverUrl: 'https://10.0.2.5:6443' ]) {
                    //sh 'kubectl get nodes'
                   //}
                sh "kubectl get nodes"
                sh "kubectl  delete  -f  k8s-deployment.yml"
                sh "kubectl  delete  -f  k8s-service.yml"
                sh "kubectl  apply  -f  k8s-deployment.yml"
                sh "kubectl  apply  -f  k8s-service.yml"
            }
            
        }
        
    }
}
