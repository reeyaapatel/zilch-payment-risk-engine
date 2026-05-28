package com.reeya.payment_risk_engine.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "PAYMENT_RISK")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRisk {

    @Id
    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_country")
    private String merchantCountry;

    @Column(name = "buyer_ip")
    private String buyerIp;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "reasons")
    private String reasons;

    @Column(name = "created_at")
    private Instant createdAt;
}

