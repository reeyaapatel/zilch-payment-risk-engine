# Payment Risk Engine

Spring Boot application for payment risk assessment.

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

## Run Flyway Migrations in dev

```bash
mvn flyway:info \
  -Dflyway.url=jdbc:h2:file:./data/riskdb \
  -Dflyway.user=sa \
  -Dflyway.password=
  
mvn flyway:clean flyway:migrate \
  -Dflyway.cleanDisabled=false \
  -Dflyway.url=jdbc:h2:file:./data/payment-risk \
  -Dflyway.user=sa
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