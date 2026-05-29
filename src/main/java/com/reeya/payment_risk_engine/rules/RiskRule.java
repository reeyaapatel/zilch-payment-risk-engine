package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskRuleResult;

public interface RiskRule {
    RiskRuleResult evaluate(PaymentRiskRequest request);
}
