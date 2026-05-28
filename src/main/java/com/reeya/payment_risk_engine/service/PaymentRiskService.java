package com.reeya.payment_risk_engine.service;
import com.reeya.payment_risk_engine.model.PaymentRisk;
import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.repository.PaymentRiskRepository;
import com.reeya.payment_risk_engine.rules.RiskRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .status(decisionStatus)
                .build();
        repository.save(decision);
        return PaymentRiskResponse.builder()
                .paymentId(request.getPaymentId())
                .status(decisionStatus)
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

