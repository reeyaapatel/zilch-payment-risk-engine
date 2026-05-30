package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskRuleResult;

/**
 * Interface for risk rules
 */
public interface RiskRule
{
    RiskRuleResult evaluate(PaymentRiskRequest request);

    String getRuleName();
}
