# Loan Amount Approval
A small service for loans

# Getting Started
Project located in github, download it using Git or manually download zip file.

## Prerequisites
To run this application there are several things needed:

Java 15 (https://adoptopenjdk.net/releases.html?variant=openjdk15&jvmVariant=hotspot)

Gradle (https://gradle.org/install/)

### Installing
Open command line or terminal, navigate to project folder, where project was Git-cloned (unpacked from zip file)

There should be _gradle.build_ file
```
/loanamountapproval/
```
Run gradle build command
```
gradle build
```
Run gradle bootRun command
```
gradle bootRun
```
Open your browser and enter following address:
```
localhost:8080/swagger-ui.html
```
## Tests
There is three unit tests using Mockito mocks. One ContextIT test just to verify that app will run.
One unit test using MockMvc to test rest calls to controller.

## Built With
* [SpringBoot](https://spring.io/projects/spring-boot) - Framework for Spring framework
* [Gradle](https://gradle.org/) - Dependency Management
* [OpenApi](https://swagger.io/specification/) - Tool that can help you design, build, document and consume REST APIs
* [Lombok](https://projectlombok.org) - Autogenerator for java classes
* [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html) - Reactive Spring library
