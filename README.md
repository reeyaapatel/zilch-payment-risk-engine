# Payment Risk Engine

A Spring Boot application for assessing payment risk before a transaction is processed.

## Project highlights

- Spring Boot REST API
- H2 database for lightweight persistence
- Flyway for database migrations
- Strategy pattern for extensible risk rules
- Caffiene as an in-memory cache for recent payment assessments

## Design choices

H2 is used to demonstrate database persistence without requiring external infrastructure. In production, this would likely be replaced with PostgreSQL or MySQL.

Flyway is included to show how database schema changes can be versioned and applied over time.

The risk rules currently use simple logic, but the design focuses on extensibility. Each rule implements the `RiskRule` interface, allowing new rules to be added without changing the core assessment flow.

A `ConcurrentHashMap` is used as a simple global cache for recently assessed payments. In production, this would likely be replaced with Redis or another distributed cache.

## Future improvements

Given a larger scope, I would add:

1. Configurable risk thresholds stored in the database which are pulled in when the application starts up. Expose and endpoint to update the in-memory store for then when update has taken place.
2. Payment risk versioning and an endpoint to update status after manual review.
3. An `isActive` flag for enabling/disabling rules dynamically.
4. Rule configuration tables for thresholds and scoring values.
5. More realistic business logic and external risk-data providers.

## Tech Stack

- Java 17
- Spring Boot
- H2 Database
- Flyway
- Maven

## Run Application

```bash
mvn spring-boot:run
```

## Run Flyway Migrations in dev only

```bash
mvn flyway:info \
  -Dflyway.url=jdbc:h2:file:./data/riskdb \
  -Dflyway.user=sa \
  -Dflyway.password=
  
  ./mvnw flyway:migrate \
  -Dflyway.url=jdbc:h2:file:./data/riskdb \
  -Dflyway.user=sa \
  -Dflyway.password=
```

## H2 Console

URL:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/riskdb
```

Username:

```text
sa
```

Password:

```text
(empty)
```

## Manually testing the endpoint

