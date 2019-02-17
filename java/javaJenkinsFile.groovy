def javastage() {
  	 
	properties = null  

	def loadProperties() {
		node {
			checkout scm
			properties = new Properties()		
			File propertiesFile = new File("${workspace}/Properties/pipeline.properties")
			properties.load(propertiesFile.newDataInputStream())    
		echo "Immediate one ${properties.appPath}"
		}
	}

	def notify(status){
		emailext(
			to: "bhanuprakash.bg@gmail.com",
			subject: "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
			body: """<p>${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' :</p>
			<p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
			
			)
	}

	node('master'){
		
		try {
			notify('Project Build Started')

			stage ('Load Properties') {
				script {
					loadProperties()				
				}
			}

			stage('Checkout') {
				git "${properties.appPath}"
				dir('utilities') {
				}			
			}	

			stage('Code Analysis') {
				sh 'mvn sonar:sonar'
			}
			
			stage('Code Coverage') {
				sh 'mvn cobertura:cobertura'
			}
			
			stage('Unit Testing') {
				sh 'mvn test'
			}

			stage('Build') {    
				sh 'mvn clean install '
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
				//sh 'ls -ltr target/*.war'
				//sh 'sudo cp target/*.war /home/devopsuser6/apache-tomcat-8.5.37/webapps'
				git "${properties.JenkinsFile}"			
				sh 'pwd'
				sh 'ls -ltr'
				sh "sudo docker build -t mytomcat:latest utilities/java"			
				sh "sudo docker run -d -p 8091:8080 --name mycontainer mytomcat"
				sh "sudo docker cp /home/devopsuser6/.jenkins/workspace/Devopspipeline/target/BankWebApp.war mycontainer:/usr/local/tomcat/webapps/"			
			}
			
			stage('Clean Workspace') {    
				sh 'pwd'
				//deleteDir()
			}

			notify('Project Build Completed')					
		} catch(err) {
			notify("Error ${err}")
			currentBuild.result='FAILURE'
		}
		
	}

}

return this
