package com.reeya.payment_risk_engine.model;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RiskRuleResult
{
    private String ruleName;
    private int score;
    private RiskLevel riskLevel;
    private String reason;
}
