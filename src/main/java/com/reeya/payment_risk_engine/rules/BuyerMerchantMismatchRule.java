package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.client.IpGeoLocationProvider;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Rule used to compare buyer and merchant country.
 */
@AllArgsConstructor
@Component
public class BuyerMerchantMismatchRule implements RiskRule {

    private static final String RULE_NAME = "BUYER_MERCHANT_MISMATCH_RULE";

    private final IpGeoLocationProvider ipGeoLocationProvider;

    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        boolean countryMatch = ipGeoLocationProvider.getCountryCode(request.getBuyerIp())
                .map(country -> country.equals(request.getMerchantCountryCode()))
                .orElse(false);

        if (countryMatch) {
            return new RiskRuleResult(RULE_NAME, 0, RiskLevel.LOW, "Buyer and merchant country match");
        }

        return new RiskRuleResult(RULE_NAME, 50, RiskLevel.MEDIUM, "Buyer and merchant country do not match");
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
