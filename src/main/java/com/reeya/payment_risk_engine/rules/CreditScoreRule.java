package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.service.CreditScoreService;
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

    private final int lowCreditScoreThreshold;

    private final int mediumCreditScoreThreshold;

    private final int lowCreditScoreRiskValue;

    private final int mediumCreditScoreRiskValue;

    public CreditScoreRule(
            CreditScoreService creditScoreService,
            @Value("${low.credit.score.risk.threshold}") int lowCreditScoreThreshold,
            @Value("${medium.credit.score.risk.threshold}") int mediumCreditScoreThreshold,
            @Value("${low.credit.risk.value}") int lowCreditScoreRiskValue,
            @Value("${medium.credit.risk.value}") int mediumCreditScoreRiskValue
    ) {
        if (lowCreditScoreThreshold <= 0
                || mediumCreditScoreThreshold <= 0
                || lowCreditScoreRiskValue <= 0
                || mediumCreditScoreRiskValue <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: must have positive thresholds and values");
        }
        if (lowCreditScoreThreshold >= mediumCreditScoreThreshold) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: Low credit score threshold must be lower than medium credit score threshold");
        }
        if (lowCreditScoreRiskValue <= mediumCreditScoreRiskValue) {
            throw new IllegalArgumentException(
                    "Invalid Credit score rule configuration: Low credit score value must be higher than medium credit score value");
        }
        this.creditScoreService = creditScoreService;
        this.lowCreditScoreThreshold = lowCreditScoreThreshold;
        this.mediumCreditScoreThreshold = mediumCreditScoreThreshold;
        this.lowCreditScoreRiskValue = lowCreditScoreRiskValue;
        this.mediumCreditScoreRiskValue = mediumCreditScoreRiskValue;
    }

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        OptionalInt creditScore = creditScoreService.getCreditScore(request.getCustomerId(), request.getBusinessDate());

        if (creditScore.isEmpty()) {
            return new RiskRuleResult(RULE_NAME, lowCreditScoreRiskValue, RiskLevel.HIGH, "Credit score not available");
        }
        if (creditScore.getAsInt() < lowCreditScoreThreshold) {
            return new RiskRuleResult(RULE_NAME, lowCreditScoreRiskValue, RiskLevel.HIGH, "Credit score is high risk");
        }

        if (creditScore.getAsInt() < mediumCreditScoreThreshold) {
            return new RiskRuleResult(RULE_NAME, mediumCreditScoreRiskValue, RiskLevel.MEDIUM, "Credit score is medium risk");
        }

        return new RiskRuleResult(RULE_NAME, 0, RiskLevel.LOW, "Credit score is low risk");

    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
