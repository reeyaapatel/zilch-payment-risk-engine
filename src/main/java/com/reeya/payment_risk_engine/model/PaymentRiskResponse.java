package com.reeya.payment_risk_engine.model;


import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentRiskResponse {

    private String paymentId;
    private PaymentRiskRequest paymentDetails;
    private Integer riskScore;
    private Status status;
    private List<String> reasons;
    private Instant createdAt;
}