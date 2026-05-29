package com.reeya.payment_risk_engine.model.api;


import com.reeya.payment_risk_engine.model.Status;
import lombok.*;

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
    private Integer version;
    private PaymentRiskRequest paymentDetails;
    private Integer riskScore;
    private Status status;
    private List<String> reasons;
    private Instant createdAt;
    private Instant lastUpdatedAt;
}
