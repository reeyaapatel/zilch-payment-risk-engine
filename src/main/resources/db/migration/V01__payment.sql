
CREATE TABLE IF NOT EXISTS PAYMENT_RISK_DECISION
(
    payment_id VARCHAR(255) PRIMARY KEY,

    amount DECIMAL(19,2) NOT NULL,

    currency VARCHAR(10) NOT NULL,

    merchant_name VARCHAR(255) NOT NULL,

    merchant_country VARCHAR(10) NOT NULL,

    buyer_ip VARCHAR(100) NOT NULL,

    risk_score INT NOT NULL,

    status VARCHAR(50) NOT NULL,

    reasons VARCHAR(2000),

    created_at TIMESTAMP NOT NULL
);

