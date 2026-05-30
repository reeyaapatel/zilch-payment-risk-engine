package com.reeya.payment_risk_engine.model.persistence;

import com.reeya.payment_risk_engine.model.Status;
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
    @Column(name = "payment_id", nullable = false, updatable = false)
    private String paymentId;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "amount", nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, updatable = false)
    private String currency;

    @Column(name = "merchant_name", nullable = false, updatable = false)
    private String merchantName;

    @Column(name = "merchant_country", nullable = false, updatable = false)
    private String merchantCountry;

    @Column(name = "buyer_ip", nullable = false, updatable = false)
    private String buyerIp;

    @Column(name = "risk_score", nullable = false, updatable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Convert(converter = StringListConverter.class)
    @Column(name = "reasons", updatable = false)
    private List<String> reasons;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
