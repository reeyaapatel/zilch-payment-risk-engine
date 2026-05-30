
package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements RiskRule {

    private static final String RULE_NAME = "HIGH_AMOUNT_RULE";

    @Value("${high.amount.high.risk.amount:1000}")
    BigDecimal highRiskAmount;

    @Value("${high.amount.review.risk.amount:500}")
    BigDecimal reviewRiskAmount;

    @Value("${high.amount.high.risk.score:10}")
    int highRiskScore;

    @Value("${high.amount.review.risk.score:5}")
    int mediumRiskScore;

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {

        if (request.getAmount().compareTo(highRiskAmount) > 0) {

            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(highRiskScore)
                    .riskLevel(RiskLevel.HIGH)
                    .reason("Amount exceeds high risk threshold")
                    .build();
        }

        else if (request.getAmount().compareTo(reviewRiskAmount) > 0) {

            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(mediumRiskScore)
                    .riskLevel(RiskLevel.MEDIUM)
                    .reason("Amount exceeds medium risk threshold")
                    .build();
        }

        return RiskRuleResult.builder()
                .ruleName(RULE_NAME)
                .score(1)
                .riskLevel(RiskLevel.LOW)
                .reason("Amount is within acceptable threshold")
                .build();
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}

