package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.client.IpGeoLocationProvider;
import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rule used to compare buyer and merchant country.
 */
@Component
public class BuyerMerchantMismatchRule implements RiskRule {

    private static final String RULE_NAME = "BUYER_MERCHANT_MISMATCH_RULE";

    private final IpGeoLocationProvider ipGeoLocationProvider;

    private final int buyerMerchantLowRiskValue;

    private final int buyerMerchantMediumRiskValue;

    public BuyerMerchantMismatchRule(
            IpGeoLocationProvider ipGeoLocationProvider,
            @Value("${buyer.merchant.rule.low.risk.value}") int buyerMerchantLowRiskValue,
            @Value("${buyer.merchant.rule.medium.risk.value}") int buyerMerchantMediumRiskValue
    )
    {
        if(ipGeoLocationProvider==null)
        {
            throw new IllegalArgumentException("Invalid configuration for BuyerMerchantMismatchRule: IpGeoLocationProvider must not be null");
        }
        if (buyerMerchantLowRiskValue<0)
        {
            throw new IllegalArgumentException("Invalid configuration for BuyerMerchantMismatchRule: buyerMerchantLowRiskValue must be positive");
        }
        if (buyerMerchantMediumRiskValue <=0)
        {
            throw new IllegalArgumentException("Invalid configuration for BuyerMerchantMismatchRule: buyerMerchantMediumRiskValue must be greater than 0");
        }
        this.ipGeoLocationProvider=ipGeoLocationProvider;
        this.buyerMerchantLowRiskValue=buyerMerchantLowRiskValue;
        this.buyerMerchantMediumRiskValue=buyerMerchantMediumRiskValue;
    }


    @Override
    public RiskRuleResult evaluate(PaymentRiskRequest request) {
        boolean countryMatch = ipGeoLocationProvider.getCountryCode(request.getBuyerIp())
                .map(country -> country.equals(request.getMerchantCountryCode()))
                .orElse(false);

        if (countryMatch) {
            return new RiskRuleResult(RULE_NAME, buyerMerchantLowRiskValue, RiskLevel.LOW, "Buyer and merchant country match");
        }

        return new RiskRuleResult(RULE_NAME, buyerMerchantMediumRiskValue, RiskLevel.MEDIUM, "Buyer and merchant country do not match");
    }

    @Override
    public String getRuleName() {
        return RULE_NAME;
    }
}
