package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.rules.RiskRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Evaluates risk rules asynchronously with a timeout and fallback result.
 */
@Service
public class AsyncRiskRuleEvaluator implements RiskRuleEvaluator {

    private final List<RiskRule> riskRules;
    private final Executor riskRuleExecutor;
    private final int fallbackScore;
    private final int timeout;

    public AsyncRiskRuleEvaluator(
            List<RiskRule> riskRules,
            Executor riskRuleExecutor,
            @Value("${payment.risk.review.threshold}") int fallbackScore,
            @Value("${payment.risk.review.timeout}") int timeout
    ) {
        if (riskRules == null || riskRules.isEmpty()) {
            throw new IllegalArgumentException("At least one risk rule must be provided");
        }
        if (riskRuleExecutor == null) {
            throw new IllegalArgumentException("Risk rule executor must be provided");
        }
        if (fallbackScore < 0 || timeout <= 0) {
            throw new IllegalArgumentException("Fallback score must be non-negative and timeout must be positive");
        }
        this.riskRules = riskRules;
        this.riskRuleExecutor = riskRuleExecutor;
        this.fallbackScore = fallbackScore;
        this.timeout = timeout;
    }

    @Override
    public List<RiskRuleResult> evaluate(PaymentRiskRequest request) {
        List<CompletableFuture<RiskRuleResult>> ruleEvaluations = riskRules.stream()
                .map(rule -> CompletableFuture
                        .supplyAsync(() -> rule.evaluate(request), riskRuleExecutor)
                        .orTimeout(timeout, TimeUnit.SECONDS)
                        .exceptionally(exception -> fallbackResult(rule)))
                .toList();

        return ruleEvaluations.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private RiskRuleResult fallbackResult(RiskRule rule) {
        return new RiskRuleResult(
                rule.getRuleName(),
                fallbackScore,
                RiskLevel.HIGH,
                "Rule failed or timed out"
        );
    }
}
