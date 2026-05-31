package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;

/**
 * Interface for risk rules.
 */
public interface RiskRule {
    RiskRuleResult evaluate(PaymentRiskRequest request);

    String getRuleName();
}
