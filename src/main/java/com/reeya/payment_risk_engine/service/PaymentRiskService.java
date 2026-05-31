package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
import com.reeya.payment_risk_engine.rules.RiskRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Service to manage payment risk assessment and status updates
 * rule execution is done asynchronously using a thread pool
 * persistence is done after the risk assessment is complete
 * Idempotency is achieved by checking if payment exists, or through database constraints if passes inital checks
 */
@Slf4j
@Service
public class PaymentRiskService {

    private final EntityManager entityManager;
    private final List<RiskRule> riskRules;
    private final Executor riskRuleExecutor;
    private final int declineThreshold;
    private final int reviewThreshold;
    private final int timeout;

    public PaymentRiskService(
            EntityManager entityManager,
            List<RiskRule> riskRules,
            Executor riskRuleExecutor,
            @Value("${payment.risk.decline.threshold}") int declineThreshold,
            @Value("${payment.risk.review.threshold}") int reviewThreshold,
            @Value("${payment.risk.review.timeout}") int timeout
    )
    {
        this.entityManager = entityManager;
        this.riskRules = riskRules;
        this.riskRuleExecutor = riskRuleExecutor;
        if (declineThreshold < 0 || reviewThreshold < 0 || timeout <= 0)
        {
            throw new IllegalArgumentException("Thresholds must be non-negative and timeout must be positive");
        }
        if (reviewThreshold >= declineThreshold)
        {
            throw new IllegalArgumentException("Review threshold must be lower than decline threshold");
        }
        this.declineThreshold = declineThreshold;
        this.reviewThreshold = reviewThreshold;
        this.timeout = timeout;
    }

    @Transactional
    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {
        Optional<PaymentRisk> existingPayment = findPaymentRisk(request.getPaymentId());
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        List<RiskRuleResult> results = evaluateRules(request);
        int riskScore = results.stream().mapToInt(RiskRuleResult::score).sum();
        List<String> reasons = results.stream().map(RiskRuleResult::reason).toList();
        PaymentRisk paymentRisk = toPaymentRisk(request, riskScore, reasons);

        try
        {
            entityManager.persist(paymentRisk);
            entityManager.flush();
        }
        catch (PersistenceException e)
        {
            log.info("Failed to persist payment risk for payment id: {}", request.getPaymentId(), e);
            return getPaymentRiskResponse(request.getPaymentId());
        }

        return toResponse(paymentRisk);
    }

    @Transactional
    public PaymentRiskResponse updateStatus(String paymentId, PaymentStatusUpdate update) {
        if (paymentId == null || paymentId.isBlank())
        {
            throw new IllegalArgumentException("Payment id is required");
        }

        PaymentRisk paymentRisk = getPaymentRiskOrThrow(paymentId);
        if (paymentRisk.getStatus() != Status.REQUIRES_REVIEW)
        {
            throw new IllegalStateException("Payment status can only be updated when it requires review");
        }

        paymentRisk.setStatus(update.status());
        paymentRisk.setLastUpdatedAt(Instant.now());
        entityManager.flush();

        return toResponse(paymentRisk);
    }

    public PaymentRiskResponse getPaymentRiskResponse(String paymentId)
    {
        return toResponse(getPaymentRiskOrThrow(paymentId));
    }

    private Optional<PaymentRisk> findPaymentRisk(String paymentId) {
        return Optional.ofNullable(entityManager.find(PaymentRisk.class, paymentId));
    }

    private PaymentRisk getPaymentRiskOrThrow(String paymentId) {
        return findPaymentRisk(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    private List<RiskRuleResult> evaluateRules(PaymentRiskRequest request) {
        List<CompletableFuture<RiskRuleResult>> ruleEvaluations = riskRules.stream()
                .map(rule -> CompletableFuture
                        .supplyAsync(() -> rule.evaluate(request), riskRuleExecutor)
                        .orTimeout(timeout, TimeUnit.SECONDS)
                        .exceptionally(exception -> new RiskRuleResult(
                                rule.getRuleName(),
                                reviewThreshold,
                                RiskLevel.HIGH,
                                "Rule failed or timed out"
                        ))
                )
                .toList();

        return ruleEvaluations.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private PaymentRisk toPaymentRisk(PaymentRiskRequest request, int riskScore, List<String> reasons) {
        Instant now = Instant.now();
        return PaymentRisk.builder()
                .paymentId(request.getPaymentId())
                .customerId(request.getCustomerId())
                .businessDate(request.getBusinessDate())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantCountryCode(request.getMerchantCountryCode())
                .merchantName(request.getMerchantName())
                .buyerIp(request.getBuyerIp())
                .status(determineDecision(riskScore))
                .riskScore(riskScore)
                .reasons(reasons)
                .createdAt(now)
                .lastUpdatedAt(now)
                .build();
    }

    private PaymentRiskResponse toResponse(PaymentRisk paymentRisk) {
        return PaymentRiskResponse.builder()
                .paymentId(paymentRisk.getPaymentId())
                .version(paymentRisk.getVersion())
                .customerId(paymentRisk.getCustomerId())
                .businessDate(paymentRisk.getBusinessDate())
                .amount(paymentRisk.getAmount())
                .currency(paymentRisk.getCurrency())
                .merchantName(paymentRisk.getMerchantName())
                .merchantCountryCode(paymentRisk.getMerchantCountryCode())
                .buyerIp(paymentRisk.getBuyerIp())
                .riskScore(paymentRisk.getRiskScore())
                .status(paymentRisk.getStatus())
                .reasons(paymentRisk.getReasons())
                .createdAt(paymentRisk.getCreatedAt())
                .lastUpdatedAt(paymentRisk.getLastUpdatedAt())
                .build();
    }

    private Status determineDecision(int score) {
        if (score >= declineThreshold) {
            return Status.DECLINED;
        }
        if (score >= reviewThreshold) {
            return Status.REQUIRES_REVIEW;
        }
        return Status.APPROVED;
    }
}
