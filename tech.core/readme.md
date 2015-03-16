# README

This repository holds shared library for appirio platform's V3 API and authentication using Dropwizard framework.

# Repositories

There are 2 core projects in this repository.

- tech.core.api -- holds shared library for other teams to use
- tech.core.sample.dropwizard -- a sample dropwizard project showing how to use shared library

# Building and Running Sample Project

See "Core Service: Development Environment Setup" document (https://docs.google.com/a/appirio.com/document/d/1e2aGnbXZgHvXZQ2GbGYksN8n0jiWxKRMbzaNc2QZBuA/edit#heading=h.ug5qzkdm27d6)

# API Shared Library Basics

API shared library have 2 goals in mind.
1. Provide methods and wrapper objects to simplify the resource(interface) development to match V3 API protocol defined in this doc (https://docs.google.com/a/appirio.com/presentation/d/15pucEI0MHj9y778EyaAWGh4MBH-I73i1-GS0ir7FhxE/edit)
2. Provide authentication library to hook with Jersey injection (dropwizard @Auth)
3. Other micro services should have ability to override or ignore the usage of shared library, in case of edge case scenario. In this case, each micro services will need to implement their own V3 protocol handling methods.

# Including Shared Library into Maven pom

Appirio platform uses Maven and custom repository (in Jenkins server) to hold our libraries and programs. Application should have following items on each of their pom files.

#### Maven Repository
Include following to register our maven repository.

    <repositories>
      <repository>
        <id>Appirio Technology Maven Repository</id>
        <url>http://maven.appirio.net:8080/</url>
        <snapshots>
          <updatePolicy>always</updatePolicy>
        </snapshots>
        <releases>
          <updatePolicy>always</updatePolicy>
        </releases>
      </repository>
    </repositories>

#### API dependency
Shared library will be included using following dependency.

    <dependencies>
      <dependency>
        <groupId>com.appirio.tech.core.api</groupId>
        <artifactId>tech.core.api</artifactId>
        <version>API-3.0.0.1-SNAPSHOT</version>
      </dependency>
    </dependencies>

# Shared Library Usage

The basic usage of shared library is explained in this section.
All terminology (like representation, resource, etc. follows dropwizard)

#### Representation class

Representation class should be simple POJO class.
The default fields that V3 pojo should have are defined in com.appirio.tech.core.api.v3.model.AbstractIdResource
Extend this class for representation where necessary.

	import com.appirio.tech.core.api.v3.model.AbstractIdResource;

	public class SamplePOJO extends AbstractIdResource {
		private String handle;
		public String getHandle() {
			return handle;
		}
		public void setHandle(String handle) {
			this.handle = handle;
		}
	}

#### Resource class

Resource class is the heart of logics, where all the requests comes in (please see dropwizard documents for more detail)

#### Request Input
Shared library has 2 interfaces for convenience, so that developers know the interface methods.
- com.appirio.tech.core.api.v3.resource.GetResource<T>
- com.appirio.tech.core.api.v3.resource.DDLResource

com.appirio.tech.core.sample.resource.SampleResource class holds the methods.
Few parameters worth noting here:
1.  @Auth AuthUser  
  * The method handles JWT authentication. Refer dropwizard authentication docs for the details.  
2. @APIFieldParam(repClass = Sample.class) FieldSelector  
  * FieldSelector holds parameters passed from request for single resource retrieval (fields parameter in V3 API).  
  * repClass is the POJO representation class. Request injection class will parse parameter according to the specified representation class.  
3. @APIQueryParam(repClass = Sample.class) QueryParameter  
  * QueryParameter holds parameters passed from request for multiple resource retrieval (@see V3 API: GET Reserved Parameters)  
  * repClass is the POJO representation class. Request injection class will parse parameter according to the specified representation class.  
4. @Valid PostPutRequest  
  * PostPutRequest holds post and put representation class as specified in V3 API doc

#### Response

All output should be wrapped to follow V3 API spec.  
1. For normal response, use ApiResponseFactory.createFieldSelectorResponse() as shown in the sample. This will wrap the representation POJO to V3 spec.  
2. For exceptions (errors), create Exception extending APIRuntimeException with appropriate status code, and throw that from application. Jersey's exception handler will catch the exception and parse to respond back with correct exception to the client.

#### Running the application

The shared library is assumed to run from main() method of com.appirio.tech.core.api.v3.dropwizard.APIApplication.  
The class will register and initiate libraries to follow V3 protocol. Also purses the class hierarchy to register Resource class that has Jersey's @Path annotation specified.  
Application has the flexibility to extend this class to provide its own initializing methods, in case the advice is always call super() for initialize() and run() so that default libraries for API is registered correctly.