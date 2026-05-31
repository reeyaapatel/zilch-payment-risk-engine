package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskRuleResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SimpleSumRiskScoreCalculator implements RiskScoreCalculator {

    @Override
    public int calculate(List<RiskRuleResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Risk rule results must not be null");
        }
        if (results.isEmpty()) {
            log.warn("No risk rule results provided, returning default score");
            return 0;
        }

        return results.stream()
                .mapToInt(RiskRuleResult::score)
                .sum();
    }
}
