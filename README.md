# Payment Risk Engine

This project demonstrates an implementation of a payment risk engine which approves payments prior to a transaction taking place.
The business logic is intentionally simplified. In a real-world platform, risk decisions would typically involve more sophisticated modelling, machine learning, and behavioural analysis. The focus of this project is to demonstrate the technical design and architecture of a risk assessment component within a payment flow.

## Design Decisions

* **Strategy Pattern** – Each risk check implements `RiskRule`, making it easy to add new rules without changing the core assessment flow.
* **Idempotency** – Payment assessments are keyed by `paymentId`, ensuring the same payment can be submitted multiple times without creating duplicate records or inconsistent outcomes. This is a common requirement in payment systems where retries may occur due to network failures or client timeouts.
* **Parallel Execution** – Risk rules are executed using `CompletableFuture` and a dedicated thread pool to simulate independent verification checks running concurrently.
* **Failure Handling** – Rule failures and timeouts generate a high-risk fallback result, ensuring uncertain payments are reviewed rather than automatically approved.
* **Persistence** – H2 and Flyway are used to demonstrate persistence and schema versioning. A production system would likely use PostgreSQL.
* **Caching** – Caffeine is used to cache external API responses and reduce repeated lookups. In a production environment, Redis would be a better choice as services are typically horizontally scaled and cached data may need to be shared across multiple application instances and consumers.
* **Configuration** – Thresholds, executor settings, and external endpoints are configurable through application properties.

## Future Improvements
If developing the project further, I would consider the following improvements:
* Store rule thresholds and configuration in the database rather than application properties.
* Support environment-specific configuration (e.g. DEV, UAT, PROD).
* Implement automatic refresh of configuration values when updated externally, avoiding application restarts.
* Add the ability to enable or disable individual risk rules through configuration. This would be useful during releases, incident management, or gradual rule rollouts.
* Support customer-specific or segment-specific risk thresholds, allowing risk models to be tuned for different customer groups.
* Add additional risk rules such as blocked merchant checks, unusual customer activity detection, and velocity checks.
* Introduce more structured logging and monitoring to improve operational visibility.
* Add authentication and authorization to secure API endpoints and ensure only trusted systems can submit or retrieve payment assessments.
* Integrate with external fraud, credit, and customer verification providers.
* Improve the database table design. E.g. Risk reasons are currently stored as a list on the payment_risk table. A more scalable approach would be to model reasons as a child table with a foreign key relationship to the payment_risk, allowing reasons to be categorised, queried independently, and filtered by importance.
* Improve overall database design. E.g. Authentication (not password), introduce database partitioning to improve performance and simplify data retention strategies and add housekeeping processes to archive or purge historical assessment data in line with retention requirements.
* Add support for manual review workflows and status updates following investigation.
* Encryption for incoming and outgoing data.


## Current Risk Rules 

- `HighAmountRule`: scores payments based on amount thresholds.
- `BuyerMerchantMismatchRule`: compares the buyer IP country code with the merchant country code.
- `CreditScoreRule`: uses cached credit-score lookups to score customer credit risk.

## Risk Scoring

Each rule returns a risk score and reason. Scores are aggregated to produce an overall risk score which determines the payment outcome:

- 0-39 → APPROVED
- 40-69 → REQUIRES_REVIEW
- 70+ → DECLINED

The thresholds and scoring model are intentionally simple and are intended to demonstrate the risk assessment flow rather than represent a production-grade fraud model.

## Run Locally

```bash
./mvnw spring-boot:run
```

File-based H2 database configured in `src/main/resources/application.properties`.

## Run Tests

```bash
./mvnw test
```

Run Checkstyle manually -- currently not enforced:

```bash
./mvnw checkstyle:check
```

## Flyway

For local development, you can run migrations against the file-based H2 database:

```bash
./mvnw flyway:migrate \
  -Dflyway.url=jdbc:h2:file:./data/riskdb \
  -Dflyway.user=sa \
  -Dflyway.password=
```

## H2 Console

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/riskdb
Username: sa
Password: empty
```

## Example Request

```bash
curl -X POST http://localhost:8080/payments/risk \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY123",
    "customerId": "CUSTOMER-001",
    "businessDate": "2026-05-30",
    "amount": 100.00,
    "currency": "GBP",
    "merchantName": "ASOS",
    "merchantCountryCode": "GB",
    "buyerIp": "1.178.94.255"
  }'
```

Update a payment after manual review:

```bash
curl -X PATCH http://localhost:8080/payments/PAY123/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED"
  }'
```

Retrieve a payment:

```bash
curl -X GET http://localhost:8080/payments/PAY123

```

## Notes

`StubCreditScoreClient`  - this is a mocked client to simulate credit score lookups. In a real system, this would be replaced with a real credit scoring service.