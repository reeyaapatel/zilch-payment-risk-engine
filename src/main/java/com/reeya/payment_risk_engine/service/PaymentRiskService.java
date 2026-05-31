package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage payment risk assessment and status updates.
 */
@Slf4j
@Service
public class PaymentRiskService {

    private final EntityManager entityManager;
    private final RiskRuleEvaluator riskRuleEvaluator;
    private final int declineThreshold;
    private final int reviewThreshold;

    public PaymentRiskService(
            EntityManager entityManager,
            RiskRuleEvaluator riskRuleEvaluator,
            @Value("${payment.risk.decline.threshold}") int declineThreshold,
            @Value("${payment.risk.review.threshold}") int reviewThreshold
    ) {
        this.entityManager = entityManager;
        this.riskRuleEvaluator = riskRuleEvaluator;
        if (declineThreshold < 0 || reviewThreshold < 0) {
            throw new IllegalArgumentException("Thresholds must be non-negative");
        }
        if (reviewThreshold >= declineThreshold) {
            throw new IllegalArgumentException("Review threshold must be lower than decline threshold");
        }
        this.declineThreshold = declineThreshold;
        this.reviewThreshold = reviewThreshold;
    }

    @Transactional
    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {
        Optional<PaymentRisk> existingPayment = findPaymentRisk(request.getPaymentId());
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        List<RiskRuleResult> results = riskRuleEvaluator.evaluate(request);
        int riskScore = results.stream().mapToInt(RiskRuleResult::score).sum();
        List<String> reasons = results.stream().map(RiskRuleResult::reason).toList();
        PaymentRisk paymentRisk = toPaymentRisk(request, riskScore, reasons);

        try {
            entityManager.persist(paymentRisk);
            entityManager.flush();
        } catch (PersistenceException e) {
            log.error("Failed to persist payment risk for payment id: {}", request.getPaymentId(), e);
            return getPaymentRiskResponse(request.getPaymentId());
        }

        return toResponse(paymentRisk);
    }

    @Transactional
    public PaymentRiskResponse updateStatus(String paymentId, PaymentStatusUpdate update) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("Payment id is required");
        }

        PaymentRisk paymentRisk = getPaymentRiskOrThrow(paymentId);
        if (paymentRisk.getStatus() != Status.REQUIRES_REVIEW) {
            throw new IllegalStateException("Payment status can only be updated when it requires review");
        }

        paymentRisk.setStatus(update.status());
        paymentRisk.setLastUpdatedAt(Instant.now());
        entityManager.flush();

        return toResponse(paymentRisk);
    }

    public PaymentRiskResponse getPaymentRiskResponse(String paymentId) {
        return toResponse(getPaymentRiskOrThrow(paymentId));
    }

    private Optional<PaymentRisk> findPaymentRisk(String paymentId) {
        return Optional.ofNullable(entityManager.find(PaymentRisk.class, paymentId));
    }

    private PaymentRisk getPaymentRiskOrThrow(String paymentId) {
        return findPaymentRisk(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
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
