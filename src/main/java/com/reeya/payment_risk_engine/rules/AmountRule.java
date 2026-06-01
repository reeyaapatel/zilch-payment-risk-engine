
package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Rule to check the payment amount against configured risk thresholds.
 */
@Component
public class AmountRule implements RiskRule {

    private static final String RULE_NAME = "AMOUNT_RULE";

    private final BigDecimal highRiskAmount;
    private final BigDecimal mediumRiskAmount;
    private final int highRiskScore;
    private final int mediumRiskScore;

    public AmountRule(
            @Value("${amount.high.risk.amount}") BigDecimal highRiskAmount,
            @Value("${amount.medium.risk.amount}") BigDecimal mediumRiskAmount,
            @Value("${amount.high.risk.score}") int highRiskScore,
            @Value("${amount.medium.risk.score}") int mediumRiskScore) {
        if (highRiskAmount == null || mediumRiskAmount == null || highRiskScore <= 0 || mediumRiskScore <= 0
                || highRiskAmount.compareTo(BigDecimal.ZERO) <= 0 || mediumRiskAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Invalid configuration for AmountRule: values must not be null and should be positive");
        }
        if (highRiskAmount.compareTo(mediumRiskAmount) <= 0) {
            throw new IllegalArgumentException(
                    "Invalid configuration for AmountRule: high risk amount should be greater than medium risk amount");
        }
        if (highRiskScore <= mediumRiskScore) {
            throw new IllegalArgumentException(
                    "Invalid configuration for AmountRule: high risk score should be higher than medium risk score");
        }
        this.highRiskAmount = highRiskAmount;
        this.mediumRiskAmount = mediumRiskAmount;
        this.highRiskScore = highRiskScore;
        this.mediumRiskScore = mediumRiskScore;
    }

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        if (request.getAmount().compareTo(highRiskAmount) > 0) {
            return new RiskRuleResult(RULE_NAME, highRiskScore, RiskLevel.HIGH, "Amount exceeds high risk threshold");
        }

        if (request.getAmount().compareTo(mediumRiskAmount) > 0) {
            return new RiskRuleResult(RULE_NAME, mediumRiskScore, RiskLevel.MEDIUM, "Amount exceeds medium risk threshold");
        }

        return new RiskRuleResult(RULE_NAME, 0, RiskLevel.LOW, "Amount is within acceptable threshold");
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
