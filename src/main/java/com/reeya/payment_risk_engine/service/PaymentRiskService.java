package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.repository.PaymentRiskRepository;
import com.reeya.payment_risk_engine.rules.RiskRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentRiskService {

    private final PaymentRiskRepository repository;
    private final List<RiskRule> riskRules;
    private final Map<String, PaymentRiskResponse> paymentRiskCache = new ConcurrentHashMap<>();

    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {
        PaymentRiskResponse cachedResponse = paymentRiskCache.get(request.getPaymentId());
        if (cachedResponse != null) {
            return cachedResponse;
        }

        List<RiskRuleResult> results = evaluateRules(request);
        int riskScore = results.stream().mapToInt(RiskRuleResult::getScore).sum();
        List<String> reasons = results.stream().map(RiskRuleResult::getReason).toList();
        PaymentRisk assessment = repository.save(toPaymentRisk(request, riskScore, reasons));
        PaymentRiskResponse response = toResponse(assessment, request);

        paymentRiskCache.put(request.getPaymentId(), response);
        return response;
    }

    public PaymentRiskResponse getPayment(String paymentId) {
        PaymentRiskResponse cachedResponse = paymentRiskCache.get(paymentId);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        PaymentRisk assessment = repository.findById(paymentId).orElseThrow();
        PaymentRiskResponse response = toResponse(assessment, toRequest(assessment));

        paymentRiskCache.put(assessment.getPaymentId(), response);
        return response;
    }

    private List<RiskRuleResult> evaluateRules(PaymentRiskRequest request) {
        return riskRules.stream()
                .map(rule -> rule.evaluate(request))
                .toList();
    }

    private PaymentRisk toPaymentRisk(PaymentRiskRequest request, int riskScore, List<String> reasons) {
        return PaymentRisk.builder()
                .paymentId(request.getPaymentId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantCountry(request.getMerchantCountry())
                .merchantName(request.getMerchantName())
                .buyerIp(request.getBuyerIp())
                .status(determineDecision(riskScore))
                .riskScore(riskScore)
                .reasons(reasons)
                .createdAt(Instant.now())
                .build();
    }

    private PaymentRiskRequest toRequest(PaymentRisk assessment) {
        return PaymentRiskRequest.builder()
                .paymentId(assessment.getPaymentId())
                .amount(assessment.getAmount())
                .currency(assessment.getCurrency())
                .merchantCountry(assessment.getMerchantCountry())
                .merchantName(assessment.getMerchantName())
                .buyerIp(assessment.getBuyerIp())
                .build();
    }

    private PaymentRiskResponse toResponse(PaymentRisk assessment, PaymentRiskRequest request) {
        return PaymentRiskResponse.builder()
                .paymentId(assessment.getPaymentId())
                .paymentDetails(request)
                .riskScore(assessment.getRiskScore())
                .status(assessment.getStatus())
                .reasons(assessment.getReasons())
                .createdAt(assessment.getCreatedAt())
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
