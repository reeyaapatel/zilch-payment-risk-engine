package com.reeya.payment_risk_engine.model;

/**
 * Output from a risk rule.
 */
public record RiskRuleResult(
        String ruleName,
        int score,
        RiskLevel riskLevel,
        String reason
) {
}
