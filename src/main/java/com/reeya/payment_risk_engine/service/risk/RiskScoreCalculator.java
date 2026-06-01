package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import java.util.List;

public interface RiskScoreCalculator {

    int calculate(List<RiskRuleResult> results);
}
