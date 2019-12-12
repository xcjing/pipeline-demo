// 需要在jenkins的Credentials设置中配置jenkins-harbor-creds、jenkins-k8s-config参数
pipeline {
    agent any
    environment {
       // HARBOR_CREDS = credentials('jenkins-harbor-creds')
        K8S_CONFIG = credentials('K8S_CONFIG')
        //GIT_TAG = sh(returnStdout: true,script: 'git describe --tags --always').trim()
        GIT_TAG = 'latest'
        HARBOR_USR = 'admin'
        HARBOR_PSW = 'Harbor12345'
    }
    parameters {
        string(name: 'HARBOR_HOST', defaultValue: '132.145.91.13:8099', description: 'harbor仓库地址')
        string(name: 'DOCKER_IMAGE', defaultValue: 'chunjin/pipeline-demo', description: 'docker镜像名')
        string(name: 'APP_NAME', defaultValue: 'pipeline-demo', description: 'k8s中标签名')
        string(name: 'K8S_NAMESPACE', defaultValue: 'demo', description: 'k8s的namespace名称')
         string(name: 'SEND_MAIL', defaultValue: 'no', description: 'mail')
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
               sh "docker login -u ${HARBOR_USR} -p ${HARBOR_PSW} ${params.HARBOR_HOST}"
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
    
    
    
    
    post{
           
        success {
             echo "success"
            script {
                if (${params.SEND_MAIL} == 'yes') {
                   
           emailext body: '''<!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        </head><table>
        <tr>
            <td><br />
            <b><font color="#0B610B">构建信息</font></b>
            <hr size="2" width="100%" align="center" /></td>
        </tr>
        <tr>
            <td>
                <ul> 
                    <li>构建名称：${JOB_NAME}</li>
                    <li>构建结果: <span style="color:green"> ${BUILD_STATUS}</span></li> 
                    <li>构建编号：${BUILD_NUMBER}  </li>
                    <li>变更记录: ${CHANGES,showPaths=true,showDependencies=true,format="<pre><ul><li>提交ID: %r</li><li>提交人：%a</li><li>提交时间：%d</li><li>提交信息：%m</li><li>提交文件：<br />%p</li></ul></pre>",pathFormat="         %p <br />"}
                </ul>
            </td>
            </tr>
            </table>
            </body>
            </html>
            ''', subject: '${PROJECT_NAME}第${BUILD_NUMBER}次构建结果:${BUILD_STATUS}', to: 'zhaojianwei@fawjiefang.com.cn,'
                }
            }
        }
         
        failure {
              echo "failure"
            script {
                if (${params.SEND_MAIL} == 'yes') {
                emailext body: '''<!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            </head>
            <body>
            <table>
            <tr>
                <td><br />
                <b><font color="#0B610B">构建信息</font></b>
                <hr size="2" width="100%" align="center" /></td>
            </tr>
            <tr>
            <td>
                <ul> 
                    <li>构建名称：${JOB_NAME}</li>
                    <li>构建结果: <span style="color:red"> ${BUILD_STATUS}</span></li>  
                    <li>构建编号：${BUILD_NUMBER}  </li>
                    <li>变更记录: ${CHANGES,showPaths=true,showDependencies=true,format="<pre><ul><li>提交ID: %r</li><li>提交人：%a</li><li>提交时间：%d</li><li>提交信息：%m</li><li>提交文件：%p</li></ul></pre>",pathFormat="%p <br />"}
                </ul>
            </td>
        </tr>
        <tr>
            <td><b><font color="#0B610B">构建日志 :</font></b>
            <hr size="2" width="100%" align="center" /></td>
        </tr>
        <tr>
            <td><textarea cols="150" rows="30" readonly="readonly"
                    style="font-family: Courier New">${BUILD_LOG}</textarea>
            </td>
        </tr>
        </table>
        </body>
        </html>
        ''', subject: '${PROJECT_NAME}第${BUILD_NUMBER}次构建结果:${BUILD_STATUS}', to: 'zhaojianwei@fawjiefang.com.cn,'
            }
            
        }
        }
    }
    
    
    
}
