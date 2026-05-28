CREATE TABLE PAYMENT_RISK
(
    payment_id       VARCHAR(30) NOT NULL,
    amount           DECIMAL NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    merchant_name    VARCHAR(300) NOT NULL,
    merchant_country VARCHAR(50) NOT NULL,
    buyer_ip         VARCHAR(10) NOT NULL,
    risk_score       INT NOT NULL,
    status           VARCHAR(10) NOT NULL,
    reasons          VARCHAR(400),
    created_at       TIMESTAMP NOT NULL,
    CONSTRAINT pk_payment_risk PRIMARY KEY (payment_id)
);