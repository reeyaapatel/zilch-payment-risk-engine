CREATE TABLE RISK_RULE_CONFIG (
    rule_name VARCHAR(100) NOT NULL,
    threshold_amount DECIMAL(19, 2) NOT NULL,
    risk_level VARCHAR(10)  NOT NULL,
    risk_score INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL
);