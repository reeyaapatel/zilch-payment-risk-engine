package com.reeya.payment_risk_engine.repository;


import com.reeya.payment_risk_engine.model.PaymentRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRiskRepository extends JpaRepository<PaymentRisk, String>
{
}
