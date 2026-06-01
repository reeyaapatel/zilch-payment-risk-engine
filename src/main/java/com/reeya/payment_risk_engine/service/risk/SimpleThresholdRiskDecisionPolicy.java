package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleThresholdRiskDecisionPolicy implements RiskDecisionPolicy {

    private final int declineThreshold;
    private final int reviewThreshold;

    public SimpleThresholdRiskDecisionPolicy(
            @Value("${risk.rule.evaluator.decline.threshold}") int declineThreshold,
            @Value("${risk.rule.evaluator.review.threshold}") int reviewThreshold
    ) {
        if (declineThreshold < 0 || reviewThreshold < 0) {
            throw new IllegalArgumentException("Thresholds must be non-negative");
        }
        if (reviewThreshold >= declineThreshold) {
            throw new IllegalArgumentException("Review threshold must be lower than decline threshold");
        }
        this.declineThreshold = declineThreshold;
        this.reviewThreshold = reviewThreshold;
    }

    @Override
    public Status determineDecision(int riskScore) {
        if (riskScore >= declineThreshold) {
            return Status.DECLINED;
        }
        if (riskScore >= reviewThreshold) {
            return Status.REQUIRES_REVIEW;
        }
        return Status.APPROVED;
    }
}
