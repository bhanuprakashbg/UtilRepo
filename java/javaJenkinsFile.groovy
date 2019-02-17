def javastage() {
  	 
		stage('Checkout') {
			//dir('app') {
				git "${properties.appPath}"						
			//}
			sh 'pwd'
			//sh '. app/'
			//sh 'pwd'
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
	
		stage('Deploy') {    
			//sh 'ls -ltr target/*.war'
			//sh 'sudo cp target/*.war /home/devopsuser6/apache-tomcat-8.5.37/webapps'
			dir ('utilities') {
			git "${properties.JenkinsFile}"			
			sh 'pwd'
			sh 'ls -ltr'
			sh "sudo docker build -t mytomcat:latest java"	
			//docker stop mycontainer || true && docker rm mycontainer || true
			sh "sudo docker run -d -p 8091:8080 --name mycontainer mytomcat"
			sh "sudo docker cp /home/devopsuser6/.jenkins/workspace/Devopspipeline/target/BankWebApp.war mycontainer:/usr/local/tomcat/webapps/"			
			}
		}
	

}

return this
