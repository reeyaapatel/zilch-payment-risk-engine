package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.Status;

public interface RiskDecisionPolicy {

    Status determineDecision(int riskScore);
}
