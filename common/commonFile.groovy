def javastage() {

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
      
  stage('Clean Workspace') {    
			sh 'pwd'
			//deleteDir()
		}
    
}


return this
