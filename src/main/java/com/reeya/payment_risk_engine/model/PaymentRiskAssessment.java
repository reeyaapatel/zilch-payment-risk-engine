package com.reeya.payment_risk_engine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_risk_assessments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRiskAssessment {
    @Id
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCountry;
    private String buyerIp;
    private String buyerCountry;
    private Integer riskScore;
    private String status;
    private String reasons;
    private Instant createdAt;
}
