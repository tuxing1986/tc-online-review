# Core microservice automation tests

This project contains automation tests for Core microservices. It has two types of api endpoints, authentication and user.

#### Included shared automation library into maven pom as dependency
  
    <dependencies>
      <dependency>
        <groupId>com.appirio.automation.api</groupId>
        <artifactId>automation-api</artifactId>
        <version>0.0.1</version>
      </dependency>
    </dependencies>



#### Resources

 - AuthenticationTest - Contains tests for authentication api endpoints.
 - UserTest - Contains tests for user api endpoints.
 - testng.xml - Reqiured to execute the tests in the above 2 test classes.
 - user.json - Data file containing test data for users.
 
 
#### Building and Running
 Jenkins Job that builds and runs the automation tests for core microservice. A test report can be seen in the Jenkins 
 with tests passed, tests failed.
https://build.appirio.net:8443/jenkins/job/test-core-api-test/


#### Hook Core Microservice Jenkins Job with Test Job
Core microservice job is hooked with the test job so that whenever build runs for core , automation tests 
are executed. 
https://build.appirio.net:8443/jenkins/job/tc-api3-lib-update/
