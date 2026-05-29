package com.reeya.payment_risk_engine.model;
import lombok.*;

import java.math.BigDecimal;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentRiskRequest
{
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCountry;
    private String buyerIp;
}
