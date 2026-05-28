
package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements RiskRule {

    private static final String RULE_NAME = "HIGH_AMOUNT_RULE";

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {

        if (request.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {

            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(10)
                    .riskLevel(RiskLevel.HIGH)
                    .reason("Amount exceeds threshold of 1000")
                    .build();
        }

        else if (request.getAmount().compareTo(BigDecimal.valueOf(500)) > 0) {

            return RiskRuleResult.builder()
                    .ruleName(RULE_NAME)
                    .score(5)
                    .riskLevel(RiskLevel.MEDIUM)
                    .reason("Amount exceeds threshold of 500")
                    .build();
        }

        return RiskRuleResult.builder()
                .ruleName(RULE_NAME)
                .score(1)
                .riskLevel(RiskLevel.LOW)
                .reason("Amount is within acceptable threshold")
                .build();
    }
}

