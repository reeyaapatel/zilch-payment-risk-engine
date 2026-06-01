package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.service.credit.CreditScoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.OptionalInt;

/**
 * Rule to check a customer's credit score against configured risk thresholds.
 */
@Component
public class CreditScoreRule implements RiskRule {

    private static final String RULE_NAME = "CREDIT_SCORE_CHECK";

    private final CreditScoreService creditScoreService;

    private final int highRiskCreditScoreThreshold;

    private final int mediumRiskCreditScoreThreshold;

    private final int highRiskCreditScoreValue;

    private final int mediumRiskCreditScoreValue;

    public CreditScoreRule(
            CreditScoreService creditScoreService,
            @Value("${credit.high.risk.score.threshold}") int highRiskCreditScoreThreshold,
            @Value("${credit.medium.risk.score.threshold}") int mediumRiskCreditScoreThreshold,
            @Value("${credit.high.risk.value}") int highRiskCreditScoreValue,
            @Value("${credit.medium.risk.value}") int mediumRiskCreditScoreValue
    ) {
        if (highRiskCreditScoreThreshold <= 0
                || mediumRiskCreditScoreThreshold <= 0
                || highRiskCreditScoreValue <= 0
                || mediumRiskCreditScoreValue <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: must have positive thresholds and values");
        }
        if (highRiskCreditScoreThreshold >= mediumRiskCreditScoreThreshold) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: High risk credit score threshold must be lower than medium risk credit score threshold");
        }
        if (highRiskCreditScoreValue <= mediumRiskCreditScoreValue) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: High risk credit score value must be higher than medium risk credit score value");
        }
        this.creditScoreService = creditScoreService;
        this.highRiskCreditScoreThreshold = highRiskCreditScoreThreshold;
        this.mediumRiskCreditScoreThreshold = mediumRiskCreditScoreThreshold;
        this.highRiskCreditScoreValue = highRiskCreditScoreValue;
        this.mediumRiskCreditScoreValue = mediumRiskCreditScoreValue;
    }

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        OptionalInt creditScore = creditScoreService.getCreditScore(request.getCustomerId(), request.getBusinessDate());

        if (creditScore.isEmpty()) {
            return new RiskRuleResult(RULE_NAME, highRiskCreditScoreValue, RiskLevel.HIGH, "Credit score not available");
        }
        if (creditScore.getAsInt() < highRiskCreditScoreThreshold) {
            return new RiskRuleResult(RULE_NAME, highRiskCreditScoreValue, RiskLevel.HIGH, "Credit score is high risk");
        }

        if (creditScore.getAsInt() < mediumRiskCreditScoreThreshold) {
            return new RiskRuleResult(RULE_NAME, mediumRiskCreditScoreValue, RiskLevel.MEDIUM, "Credit score is medium risk");
        }

        return new RiskRuleResult(RULE_NAME, 0, RiskLevel.LOW, "Credit score is low risk");

    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
