# Customer Care

This is a sample RESTful web application developed using Spring Boot.

It allows to perform basic CRUD operations on _Customers_ and their associated _Devices_.

## Requirements

- Java 19 or later
- Maven 3.8 or later

## Setup

From the root folder of the project, run:

```shell
mvn spring-boot:run
```

Alternatively, build the project with `mvn verify` and then manually run the produced
jar with:

```shell
java -jar target/customer-care-1.0.jar
```

The server will be accessible under `http://localhost:8080`.

## Swagger / OpenAPI

- [/swagger-ui.html](http://localhost:8080/swagger-ui.html) exposes a Swagger UI front-end.
- The OpenAPI specification for the project can be found at:
    - [/v3/api-docs](http://localhost:8080/v3/api-docs) for the JSON
      version
    - [/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) for the YAML version

## Configuration

The main configuration file for the project
is [application.yaml](src/main/resources/application.yaml).

### Debugging

When
the `debug` [Spring profile](https://docs.spring.io/spring-boot/docs/3.0.2/reference/html/features.html#features.profiles)
is enabled, more detailed information will be logged on the console.

In addition, an H2 console will be exposed at [/h2-console](http://localhost:8080/h2-console).
The necessary information to access it will be printed while the application is starting (
search `H2ConsoleAutoConfiguration`).

See [application-debug.yaml](src/main/resources/application-debug.yaml) for more details.

### Database

By default, an in-memory H2 database is used, with automatic JPA schema initialization on startup.

You can use your own, custom SQL database by changing the properties
in [application.yaml](src/main/resources/application.yaml). Refer to [Spring
Boot's reference docs on DataSource configuration](https://docs.spring.io/spring-boot/docs/3.0.2/reference/html/data.html#data.sql.datasource.configuration)
for more information.

If you want to initialize the schema by yourself, refer to the entities defined under
the [io.jacopocav.customercare.model](src/main/java/io/jacopocav/customercare/model) package for the
required columns and relationships.

## Coverage Report

The project is configured to automatically generate a JaCoCo code coverage report
under `target/site/jacoco` during the Maven `test` phase.