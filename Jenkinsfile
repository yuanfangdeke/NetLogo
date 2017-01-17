node {
  stage('Build') { // <2>
    def workspace = pwd()
    sh '${workspace}/sbt update'
    sh '${workspace}/sbt all'
  }
  stage('Test') {
    def workspace = pwd()
    sh '${workspace}/sbt depend'
    sh '${workspace}/sbt headless/depend'
    sh '${workspace}/sbt netlogo/test:fast'
    sh '${workspace}/sbt parserJS/test'
    sh '${workspace}/sbt nogen netlogo/test:fast'
    sh '${workspace}/sbt threed netlogo/test:fast'
    sh '${workspace}/sbt headless/test:fast'
    sh '${workspace}/sbt netlogo/test:medium'
    sh '${workspace}/sbt nogen netlogo/test:medium'
    sh '${workspace}/sbt headless/test:medium'
    sh '${workspace}/sbt nogen headless/test:medium'
    sh '${workspace}/sbt netlogo/test:slow'
    sh '${workspace}/sbt threed netlogo/test:slow'
    sh '${workspace}/sbt netlogo/test:extensionTests'
    junit 'netlogo-gui/target/test-reports/*.xml'
  }
}
