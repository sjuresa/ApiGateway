# API gateway

## Introduction

## Building

To build you can just :

* `mvn clean package` does not run any test

## Running

You can package a WAR file and deploy it on your application server.
 

But you can also use other forms of packaging to execute this services

### WildFly Swarm

Package the samples with WildFly Swarm using the following Maven command :

* `mvn clean package -Pwildfly-swarm`

This will create an executable JAR under the `target` directory. Just execute it with `java -jar target/ApiGateway-1.0-SNAPSHOT-swarm.jar` and invoke the microservice at http://localhost:8080/user 

## Registration


### Get registration

Get registration data for registered RA user. Response 200 if user has RA role, else 404 Not Found.

#### Post Registration

Post registration data for new / edit user. Response 201 if user has RA role, else 403 - Forbidden. If user with attribute email allready exsists, return 409 - Conflict.

## Self-registration

### Get self registration

Get registration data. Response 200.

### Post Registration

Post registration data for new user. Response 201 if ok. If user with attribute email allready exsists, return 409 - Conflict.

