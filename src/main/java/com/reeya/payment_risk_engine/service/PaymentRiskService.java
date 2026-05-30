package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
import com.reeya.payment_risk_engine.rules.RiskRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentRiskService {


    @PersistenceContext
    private final EntityManager entityManager;

    private final List<RiskRule> riskRules;

    @Transactional
    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {
        Optional<PaymentRisk> existingPayment = findPaymentRisk(request.getPaymentId());
        if (existingPayment.isPresent()) {
            return toResponse(existingPayment.get());
        }

        List<RiskRuleResult> results = evaluateRules(request);
        int riskScore = results.stream().mapToInt(RiskRuleResult::getScore).sum();
        List<String> reasons = results.stream().map(RiskRuleResult::getReason).toList();
        PaymentRisk paymentRisk = toPaymentRisk(request, riskScore, reasons);

        try
        {
            entityManager.persist(paymentRisk);
            entityManager.flush();
        }
        catch (PersistenceException e)
        {
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

        paymentRisk.setStatus(update.getStatus());
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
        return riskRules.stream()
                .map(rule -> rule.evaluate(request))
                .toList();
    }

    private PaymentRisk toPaymentRisk(PaymentRiskRequest request, int riskScore, List<String> reasons) {
        Instant now = Instant.now();
        return PaymentRisk.builder()
                .paymentId(request.getPaymentId())
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
        if (score >= 70) {
            return Status.DECLINED;
        }
        if (score >= 40) {
            return Status.REQUIRES_REVIEW;
        }
        return Status.APPROVED;
    }
}
