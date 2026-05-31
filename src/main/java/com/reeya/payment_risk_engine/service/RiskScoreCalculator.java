package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskRuleResult;
import java.util.List;

public interface RiskScoreCalculator {

    int calculate(List<RiskRuleResult> results);
}
