package com.reeya.payment_risk_engine.service;
import com.reeya.payment_risk_engine.model.PaymentRiskDecision;
import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.PaymentRiskResponse;
import com.reeya.payment_risk_engine.repository.PaymentRiskDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRiskService {

    private final PaymentRiskDecisionRepository repository;

    public PaymentRiskResponse assessRisk(PaymentRiskRequest request) {

        PaymentRiskDecision decision = new PaymentRiskDecision();
        repository.save(decision);
        return PaymentRiskResponse.builder()
                .build();
    }

    public PaymentRiskResponse getPayment(String paymentId) {

        PaymentRiskDecision assessment = repository.findById(paymentId).orElseThrow();
        return PaymentRiskResponse.builder()
                .build();
    }


}

