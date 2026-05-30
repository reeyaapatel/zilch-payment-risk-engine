package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.service.credit.CreditScoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rule to check credit score
 * -- this would be a call to a credit scoring service instead of a mock and would be able to be
 * -- credit scores threshold values are configurable but would need services to be bounced
 */
@Component
public class CreditScoreRule implements RiskRule {

    private static final String RULE_NAME = "CREDIT_SCORE_CHECK";

    private final CreditScoreService creditScoreService;

    private final int highCreditScoreThreshold;

    private final int mediumCreditScoreThreshold;


    public CreditScoreRule
            (
                    CreditScoreService creditScoreService,
                    @Value("${high.credit.risk.value:500}") int highCreditScoreThreshold,
                    @Value("${medium.credit.risk.value:650}") int mediumCreditScoreThreshold
            ) {
        this.creditScoreService = creditScoreService;
        this.highCreditScoreThreshold = highCreditScoreThreshold;
        this.mediumCreditScoreThreshold = mediumCreditScoreThreshold;
    }

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        int creditScore = creditScoreService.getCreditScore(request.getCustomerId(), request.getBusinessDate());

        if (creditScore < highCreditScoreThreshold) {
            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(50)
                    .riskLevel(RiskLevel.HIGH)
                    .reason("Credit score is high risk")
                    .build();
        }

        if (creditScore < mediumCreditScoreThreshold) {
            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(20)
                    .riskLevel(RiskLevel.MEDIUM)
                    .reason("Credit score is medium risk")
                    .build();
        }

        return RiskRuleResult.builder()
                .ruleName(RULE_NAME)
                .score(0)
                .riskLevel(RiskLevel.LOW)
                .reason("Credit score is low risk")
                .build();
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
