package com.reeya.payment_risk_engine.service;
import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.repository.PaymentRiskRepository;
import com.reeya.payment_risk_engine.rules.RiskRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRiskService {

    private final PaymentRiskRepository repository;
    private final List<RiskRule> riskRules;

    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {

        List<RiskRuleResult> results = riskRules.stream().map(rule -> rule.evaluate(request)).toList();

        int riskScore = results.stream().mapToInt(RiskRuleResult::getScore).sum();
        Status decisionStatus = determineDecision(riskScore);
        PaymentRisk decision = PaymentRisk.builder()
                .paymentId(request.getPaymentId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantCountry(request.getMerchantCountry())
                .merchantName(request.getMerchantName())
                .buyerIp(request.getBuyerIp())
                .status(decisionStatus)
                .riskScore(riskScore)
                .reasons(results.stream().map(RiskRuleResult::getReason).toList())
                .createdAt(Instant.now())
                .build();

        repository.save(decision);
        //
        // todo: error handling for duplicate payments

        return PaymentRiskResponse.builder()
                .paymentId(request.getPaymentId())
                .paymentDetails(request)
                .status(decisionStatus)
                .reasons(results.stream().map(RiskRuleResult::getReason).toList())
                .createdAt(decision.getCreatedAt())
                .build();
    }

    private Status determineDecision(int score) {

        if(score >= 70) {
            return Status.DECLINED;
        }

        if(score >= 40) {
            return Status.REQUIRES_REVIEW;
        }

        return Status.APPROVED;
    }


    public PaymentRiskResponse getPayment(String paymentId) {

        PaymentRisk assessment = repository.findById(paymentId).orElseThrow();
        return PaymentRiskResponse.builder()
                .build();
    }


}

