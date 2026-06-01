package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AmountRuleTest {

    private AmountRule amountRule;

    @BeforeEach
    public void setUp() {
        amountRule = new AmountRule(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), 10, 5);
    }

    @ParameterizedTest
    @MethodSource("amountRiskCases")
    public void evaluate_shouldReturnExpectedRisk(
            BigDecimal amount,
            RiskRuleResult expectedResult
    ) {
        PaymentRiskRequest request = paymentRiskRequest(amount);

        RiskRuleResult result = amountRule.evaluate(request);

        assertEquals(expectedResult, result);
    }

    @Test
    public void constructor_whenAmountsAreNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AmountRule(null, BigDecimal.valueOf(500), 10, 5)
        );

        // THEN
        assertEquals(
                "Invalid configuration for AmountRule: values must not be null and should be positive",
                exception.getMessage());
    }

    @Test
    public void constructor_whenScoresAreNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AmountRule(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), 0, 5)
        );

        // THEN
        assertEquals(
                "Invalid configuration for AmountRule: values must not be null and should be positive",
                exception.getMessage());
    }

    @Test
    public void constructor_whenHighRiskAmountIsNotGreaterThanMediumRiskAmountThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AmountRule(BigDecimal.valueOf(500), BigDecimal.valueOf(500), 10, 5)
        );

        // THEN
        assertEquals(
                "Invalid configuration for AmountRule: high risk amount should be greater than medium risk amount",
                exception.getMessage());
    }

    @Test
    public void constructor_whenHighRiskScoreIsNotGreaterThanMediumRiskScoreThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AmountRule(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), 5, 5)
        );

        // THEN
        assertEquals(
                "Invalid configuration for AmountRule: high risk score should be higher than medium risk score",
                exception.getMessage());
    }

    private static Stream<Arguments> amountRiskCases() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(1000.01), new RiskRuleResult(
                        "AMOUNT_RULE",
                        10,
                        RiskLevel.HIGH,
                        "Amount exceeds high risk threshold"
                )),
                Arguments.of(BigDecimal.valueOf(1000), new RiskRuleResult(
                        "AMOUNT_RULE",
                        5,
                        RiskLevel.MEDIUM,
                        "Amount exceeds medium risk threshold"
                )),
                Arguments.of(BigDecimal.valueOf(750), new RiskRuleResult(
                        "AMOUNT_RULE",
                        5,
                        RiskLevel.MEDIUM,
                        "Amount exceeds medium risk threshold"
                )),
                Arguments.of(BigDecimal.valueOf(500.01), new RiskRuleResult(
                        "AMOUNT_RULE",
                        5,
                        RiskLevel.MEDIUM,
                        "Amount exceeds medium risk threshold"
                )),
                Arguments.of(BigDecimal.valueOf(500), new RiskRuleResult(
                        "AMOUNT_RULE",
                        0,
                        RiskLevel.LOW,
                        "Amount is within acceptable threshold"
                ))
        );
    }

    private PaymentRiskRequest paymentRiskRequest(BigDecimal amount) {
        return PaymentRiskRequest.builder()
                .paymentId("payment-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(amount)
                .currency("USD")
                .merchantName("ASOS")
                .merchantCountryCode("US")
                .buyerIp("127.0.0.1")
                .build();
    }
}
