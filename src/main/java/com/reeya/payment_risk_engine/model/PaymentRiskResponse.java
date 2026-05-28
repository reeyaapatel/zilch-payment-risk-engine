package com.reeya.payment_risk_engine.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRiskResponse {

    private String paymentId;
    private PaymentRiskRequest paymentDetails;
    private Integer riskScore;
    private String status;
    private List<String> reasons;
}