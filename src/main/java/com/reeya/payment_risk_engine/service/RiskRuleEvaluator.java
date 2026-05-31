package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;

import java.util.List;

/**
 * Evaluates risk rules for a payment risk request.
 */
public interface RiskRuleEvaluator {

    List<RiskRuleResult> evaluate(PaymentRiskRequest request);
}
