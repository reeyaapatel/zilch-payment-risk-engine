package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.client.IpGeoLocationProvider;
import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BuyerMerchantMismatchRuleTest {

    @Mock
    private IpGeoLocationProvider ipGeoLocationProvider;

    private BuyerMerchantMismatchRule rule;

    @BeforeEach
    public void setUp() {
        rule = new BuyerMerchantMismatchRule(ipGeoLocationProvider,0, 50);
    }

    @ParameterizedTest
    @MethodSource("buyerMerchantCases")
    public void evaluate_shouldReturnExpectedRisk(
            Optional<String> buyerCountryCode,
            String merchantCountryCode,
            RiskRuleResult expectedResult
    ) {
        PaymentRiskRequest request = paymentRiskRequest(merchantCountryCode);
        Mockito.when(ipGeoLocationProvider.getCountryCode("1.2.3.4")).thenReturn(buyerCountryCode);

        RiskRuleResult result = rule.evaluate(request);

        assertEquals(expectedResult, result);
        Mockito.verify(ipGeoLocationProvider).getCountryCode("1.2.3.4");
        Mockito.verifyNoMoreInteractions(ipGeoLocationProvider);
    }

    @Test
    public void constructor_whenIpGeoLocationProviderIsNullThrowsError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BuyerMerchantMismatchRule(null, 0, 50)
        );

        assertEquals(
                "Invalid configuration for BuyerMerchantMismatchRule: IpGeoLocationProvider must not be null",
                exception.getMessage());
    }

    @Test
    public void constructor_whenLowRiskValueIsNegativeThrowsError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BuyerMerchantMismatchRule(ipGeoLocationProvider, -1, 50)
        );

        assertEquals(
                "Invalid configuration for BuyerMerchantMismatchRule: buyerMerchantLowRiskValue must be positive",
                exception.getMessage());
    }

    @Test
    public void constructor_whenMediumRiskValueIsNotPositiveThrowsError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BuyerMerchantMismatchRule(ipGeoLocationProvider, 0, 0)
        );

        assertEquals(
                "Invalid configuration for BuyerMerchantMismatchRule: buyerMerchantMediumRiskValue must be greater than 0",
                exception.getMessage());
    }

    private static Stream<Arguments> buyerMerchantCases() {
        return Stream.of(
                Arguments.of(Optional.of("GB"), "GB", new RiskRuleResult(
                        "BUYER_MERCHANT_MISMATCH_RULE",
                        0,
                        RiskLevel.LOW,
                        "Buyer and merchant country match"
                )),
                Arguments.of(Optional.of("US"), "GB", new RiskRuleResult(
                        "BUYER_MERCHANT_MISMATCH_RULE",
                        50,
                        RiskLevel.MEDIUM,
                        "Buyer and merchant country do not match"
                )),
                Arguments.of(Optional.empty(), "GB", new RiskRuleResult(
                        "BUYER_MERCHANT_MISMATCH_RULE",
                        50,
                        RiskLevel.MEDIUM,
                        "Buyer and merchant country do not match"
                ))
        );
    }

    private PaymentRiskRequest paymentRiskRequest(String merchantCountryCode) {
        return PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("ASOS")
                .merchantCountryCode(merchantCountryCode)
                .buyerIp("1.2.3.4")
                .build();
    }
}
