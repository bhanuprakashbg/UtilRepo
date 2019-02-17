def javastage() {
  	 
		stage('Checkout') {			
			git "${properties.appPath}"									
		}	

		stage('Code Analysis') {
			withSonarQubeEnv('My SonarQube Server') {
				sh 'mvn sonar:sonar'
			}
			
			timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
    				def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
    				if (qg.status != 'OK') {
      					error "Pipeline aborted due to quality gate failure: ${qg.status}"
    				}
  			}
		}		
	
		stage('Code Coverage') {
			sh 'mvn cobertura:cobertura'
		}

		stage('Unit Testing') {
			sh 'mvn test'
		}

		stage('Build') {    
			sh 'mvn clean install'
		}
		
		stage ('Artifactory') {
			def server = Artifactory.newServer url:"${properties.artifactoryURL}", username: "${properties.artifactoryUsername}", password: "${properties.artifactoryPassword}"
			def uploadSpec = """{
			"files": [
			{
			"pattern": "*.war",
			"target": "lib-release"
			}
			]
			}""" 
			def buildInfo = Artifactory.newBuildInfo()
			server.upload spec: uploadSpec, buildInfo: buildInfo
			server.publishBuildInfo buildInfo
		}

		stage('Deploy') {    			
			dir ('utilities') {
			git "${properties.JenkinsFile}"						
			sh "sudo docker build -t mytomcat:latest java"	
			sh "sudo docker stop mycontainer || true && sudo docker rm mycontainer || true"
			sh "sudo docker run -d -p 8091:8080 --name mycontainer mytomcat"
			sh "sudo docker cp /home/devopsuser6/.jenkins/workspace/Devopspipeline/target/BankWebApp.war mycontainer:/usr/local/tomcat/webapps/"			
			}
		}
	
		stage('Clean Workspace') {    
			sh 'pwd'
			deleteDir()
		}

}

return this
