package com.reeya.payment_risk_engine.model.api;


import com.reeya.payment_risk_engine.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Payment risk API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRiskResponse {

    private String paymentId;
    private Integer version;
    private String customerId;
    private LocalDate businessDate;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCountryCode;
    private String buyerIp;
    private Integer riskScore;
    private Status status;
    private List<String> reasons;
    private Instant createdAt;
    private Instant lastUpdatedAt;
}
