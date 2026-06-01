CREATE TABLE PAYMENT_RISK
(
    payment_id       VARCHAR(30) NOT NULL,
    version          INT NOT NULL,
    customer_id      VARCHAR(50) NOT NULL,
    business_date    DATE NOT NULL,
    amount           DECIMAL NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    merchant_name    VARCHAR(300) NOT NULL,
    merchant_country_code VARCHAR(2) NOT NULL,
    buyer_ip         VARCHAR(45) NOT NULL,
    risk_score       INT NOT NULL,
    status           VARCHAR(30) NOT NULL,
    reasons          VARCHAR(1000),
    created_at       TIMESTAMP NOT NULL,
    last_updated_at   TIMESTAMP NOT NULL,
    CONSTRAINT pk_payment_risk PRIMARY KEY (payment_id)
-- apply grants when full schema with users is created
);
