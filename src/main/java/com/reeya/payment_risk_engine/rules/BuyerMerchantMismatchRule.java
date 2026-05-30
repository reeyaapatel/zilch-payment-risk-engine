package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.client.IpGeoLocationClient;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;



@AllArgsConstructor
@Component
public class BuyerMerchantMismatchRule implements RiskRule{

    private static final String RULE_NAME = "BUYER_MERCHANT_MISMATCH_RULE";

    private final IpGeoLocationClient client;

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        boolean countryMatch = client.getCountryCode(request.getBuyerIp()).map(country -> country.equals(request.getMerchantCountryCode())).orElse(false);
        if (countryMatch)
            return new RiskRuleResult(RULE_NAME, 0, RiskLevel.LOW, "Buyer and merchant country match");
        else
            return new RiskRuleResult(RULE_NAME, 50, RiskLevel.MEDIUM, "Buyer and merchant country do not match");

    }
}
