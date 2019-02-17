def javastage() {
  	 
		stage('Checkout') {
			git "${properties.appPath}"						
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
	

}

return this
