package com.reeya.payment_risk_engine.repository;


import com.reeya.payment_risk_engine.model.PaymentRiskDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRiskDecisionRepository extends JpaRepository<PaymentRiskDecision, String> {
}
