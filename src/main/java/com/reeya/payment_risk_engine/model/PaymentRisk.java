package com.reeya.payment_risk_engine.model;

import com.reeya.payment_risk_engine.model.functions.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "PAYMENT_RISK")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRisk {

    @Id
    @Column(name = "payment_id", updatable = false)
    private String paymentId;

    @Column(name = "amount", updatable = false)
    private BigDecimal amount;

    @Column(name = "currency", updatable = false)
    private String currency;

    @Column(name = "merchant_name", updatable = false)
    private String merchantName;

    @Column(name = "merchant_country", updatable = false)
    private String merchantCountry;

    @Column(name = "buyer_ip", updatable = false)
    private String buyerIp;

    @Column(name = "risk_score", updatable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", updatable = false)
    private Status status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "reasons", updatable = false)
    private List<String> reasons;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

