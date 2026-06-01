# Payment Risk Engine

This project demonstrates an implementation of a payment risk engine which approves payments prior to a transaction taking place.
The business logic is intentionally simplified. In a real-world platform, risk decisions would typically involve more sophisticated modelling, machine learning, and behavioural analysis. The focus of this project is to demonstrate the technical design and architecture of a risk assessment component within a payment flow.

## Design Decisions

* **SOLID/Strategy Pattern** – The design implements SOLID principles by depending on abstractions rather than concrete implementations. `RiskRule` allows new rules to be added without changing the core service flow, supporting the Open/Closed Principle. `PaymentRiskService` depends on interfaces such as `RiskRuleEvaluator`, `RiskDecisionPolicy`, and `RiskScoreCalculator`, demonstrating the Dependency Inversion Principle and keeping rule execution, scoring, and decision logic separate.
* **Idempotency** – Payment risk decisions are keyed by `paymentId`, ensuring the same payment can be submitted multiple times without creating duplicate records or inconsistent outcomes. This is a common requirement in payment systems where retries may occur due to network failures or client timeouts.
* **Parallel Execution** – Risk rules are executed using `CompletableFuture` and a dedicated thread pool to simulate independent verification checks running concurrently.
* **Failure Handling** – Rule failures and timeouts generate a high-risk fallback result, ensuring uncertain payments are reviewed rather than automatically declined or not being processed due to individual rule errors.
* **Persistence** – H2 and Flyway are used to demonstrate persistence and schema versioning. A production system would likely use PostgreSQL.
* **Caching** – Caffeine is used as a cache for credit score lookups. In a production environment, Redis would be a better choice as services are typically horizontally scaled and cached data may need to be shared across multiple application instances and consumers.
* **Configuration** – Thresholds, executor settings, and external endpoints are configurable through application properties.
* **Authentication** – API endpoints are secured with basic authentication for demonstration. In production, authentication and authorization would typically be handled via an API gateway using OAuth2/JWT and service-to-service authentication.

## Process Flow

High-level overview of the payment risk assessment process:
```text
API Client
    ↓
PaymentRiskController
    ↓
PaymentRiskService
    ↓
Risk Rules (executed in parallel)
    ├─ Amount Rule
    ├─ Buyer/Merchant Location Rule ──► IP Geolocation API
    └─ Credit Score Rule ─────────────► Credit Score Service
                                            ↓
                                         Caffeine Cache (for credit score lookups)

    ↓
RiskScoreCalculator
    ↓
RiskDecisionPolicy
    ↓
Persist PaymentRisk
    ↓
Response
```

## Current Risk Rules
The current implementation includes three risk rules:
- `AmountRule`: scores payments based on amount thresholds.
- `BuyerMerchantMismatchRule`: compares the buyer IP country code with the merchant country code.
- `CreditScoreRule`: uses cached credit-score lookups to score customer credit risk.

`SimpleSumRiskScoreCalculator` and `SimpleThresholdRiskDecisionPolicy` are used to calculate and apply risk scores.

## Future Improvements

### Risk Engine & Business Logic

* Store rule thresholds and configuration externally rather than in application properties.
* Add the ability to enable or disable individual risk rules through configuration. This would be useful during releases, incident management, or gradual rule rollouts.
* Support customer-specific or segment-specific risk thresholds, allowing risk models to be tuned for different customer groups.
* Enhance the risk scoring model. The current implementation uses simple score aggregation; a production system would likely use weighted scoring per rule.
* Add additional risk rules such as blocked merchant checks, unusual customer activity detection, and velocity checks.
* Integrate with multiple external fraud, credit, and customer verification providers.

### Configuration, Deployment & Monitoring

* Support environment-specific configuration (e.g. DEV, UAT, PROD).
* Implement automatic refresh of configuration values when updated externally, avoiding application restarts.
* Introduce more structured logging and monitoring to improve operational visibility.
* Containerize the application to simplify deployment and operational management.
* Improve error handling with custom exceptions and error responses.

### Resilience & Performance

* Further optimise the cache strategies for recent payments and all frequently accessed external API responses with appropriate expiration policies.
* Implement specific timeout and retry policies for external API calls.


### Security

* Improve authentication and authorisation to secure API endpoints and ensure only trusted systems can submit or retrieve payment assessments.
* Add encryption for incoming and outgoing data.

### Database Design

* Improve the database table design. Risk reasons are currently stored as a list on the `payment_risk` table. A more scalable approach would be to model reasons as a child table with a foreign key relationship to the assessment, allowing reasons to be categorised, queried independently, and filtered by importance.
* Improve overall database design by introducing partitioning to improve performance and simplify data retention strategies.
* Add housekeeping processes to archive or purge historical assessment data in line with retention requirements.

### Workflow & Lifecycle Management

* Add support for manual review workflows and status updates following investigation.


## Run Locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
or via IntelliJ IDEA run config with -Dspring-boot.run.profiles=dev program args

File-based H2 database configured in `src/main/resources/application-dev.properties`.

## Run Tests

```bash
./mvnw test
```

Run Checkstyle manually; it is currently not enforced:

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
When accessing H2 use admin/password credentials.
```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/riskdb
Username: sa
Password: <empty>
```

## Example Request

POST a new payment for risk assessment:
```bash
curl -u admin:password \
  -X POST http://localhost:8080/payments/risk \
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

PATCH a payment after manual review:

```bash
curl -u admin:password \
  -X PATCH http://localhost:8080/payments/PAY123/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED"
  }'
```

GET a payment:

```bash
curl -u admin:password http://localhost:8080/payments/PAY123
```

Expected response:
```json
{"paymentId":"PAY123","version":1,"customerId":"CUSTOMER-001","businessDate":"2026-05-30","amount":100.00,"currency":"GBP","merchantName":"ASOS","merchantCountryCode":"GB","buyerIp":"1.178.94.255","riskScore":1,"status":"APPROVED","reasons":["Buyer and merchant country match","Credit score is low risk","Amount is within acceptable threshold"],"createdAt":"2026-05-30T19:37:19.769347Z","lastUpdatedAt":"2026-05-30T19:37:19.769347Z"}
```

## Notes

`StubCreditScoreClient` - this is a stub client used to simulate credit score lookups. In a real system, this would be replaced with a real credit scoring service.
