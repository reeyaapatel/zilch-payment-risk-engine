package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.Status;

public interface RiskDecisionPolicy {

    Status determineDecision(int riskScore);
}
