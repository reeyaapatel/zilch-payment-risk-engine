package com.reeya.payment_risk_engine.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRiskRequest
{
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCountry;
    private String buyerIp;
    private String buyerCountry;
}
