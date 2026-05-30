package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.client.IpGeoLocationClient;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BuyerMerchantMismatchRuleTest {

    @Mock
    private IpGeoLocationClient ipGeoLocationClient;

    private BuyerMerchantMismatchRule rule;

    @BeforeEach
    void setUp() {
        rule = new BuyerMerchantMismatchRule(ipGeoLocationClient);
    }

    @Test
    void evaluate_shouldReturnLowRisk_whenBuyerCountryMatchesMerchantCountry() {
        PaymentRiskRequest request = paymentRiskRequest("GB");
        Mockito.when(ipGeoLocationClient.getCountryCode("1.2.3.4")).thenReturn(Optional.of("GB"));

        RiskRuleResult result = rule.evaluate(request);

        assertEquals("BUYER_MERCHANT_MISMATCH_RULE", result.getRuleName());
        assertEquals(0, result.getScore());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertEquals("Buyer and merchant country match", result.getReason());
        Mockito.verify(ipGeoLocationClient).getCountryCode("1.2.3.4");
        Mockito.verifyNoMoreInteractions(ipGeoLocationClient);
    }

    @Test
    void evaluate_shouldReturnMediumRisk_whenBuyerCountryDoesNotMatchMerchantCountry() {
        PaymentRiskRequest request = paymentRiskRequest("GB");
        Mockito.when(ipGeoLocationClient.getCountryCode("1.2.3.4")).thenReturn(Optional.of("US"));

        RiskRuleResult result = rule.evaluate(request);

        assertEquals("BUYER_MERCHANT_MISMATCH_RULE", result.getRuleName());
        assertEquals(50, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertEquals("Buyer and merchant country do not match", result.getReason());
        Mockito.verify(ipGeoLocationClient).getCountryCode("1.2.3.4");
        Mockito.verifyNoMoreInteractions(ipGeoLocationClient);
    }

    @Test
    void evaluate_shouldReturnMediumRisk_whenBuyerCountryIsUnknown() {
        PaymentRiskRequest request = paymentRiskRequest("GB");
        Mockito.when(ipGeoLocationClient.getCountryCode("1.2.3.4")).thenReturn(Optional.empty());

        RiskRuleResult result = rule.evaluate(request);

        assertEquals("BUYER_MERCHANT_MISMATCH_RULE", result.getRuleName());
        assertEquals(50, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertEquals("Buyer and merchant country do not match", result.getReason());
        Mockito.verify(ipGeoLocationClient).getCountryCode("1.2.3.4");
        Mockito.verifyNoMoreInteractions(ipGeoLocationClient);
    }

    private PaymentRiskRequest paymentRiskRequest(String merchantCountryCode) {
        return PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("ASOS")
                .merchantCountryCode(merchantCountryCode)
                .buyerIp("1.2.3.4")
                .build();
    }
}
