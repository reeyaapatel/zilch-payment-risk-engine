package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HighAmountRuleTest {

    private HighAmountRule highAmountRule;

    @BeforeEach
    void setUp() {
        highAmountRule = new HighAmountRule(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), 10, 5);
    }

    @ParameterizedTest
    @MethodSource("amountRiskCases")
    void evaluate_shouldReturnExpectedRisk(
            BigDecimal amount,
            int expectedScore,
            RiskLevel expectedRiskLevel,
            String expectedReason
    ) {
        PaymentRiskRequest request = paymentRiskRequest(amount);

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(expectedScore, result.getScore());
        assertEquals(expectedRiskLevel, result.getRiskLevel());
        assertEquals(expectedReason, result.getReason());
    }

    private static Stream<Arguments> amountRiskCases() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(1000.01), 10, RiskLevel.HIGH, "Amount exceeds high risk threshold"),
                Arguments.of(BigDecimal.valueOf(1000), 5, RiskLevel.MEDIUM, "Amount exceeds medium risk threshold"),
                Arguments.of(BigDecimal.valueOf(750), 5, RiskLevel.MEDIUM, "Amount exceeds medium risk threshold"),
                Arguments.of(BigDecimal.valueOf(500.01), 5, RiskLevel.MEDIUM, "Amount exceeds medium risk threshold"),
                Arguments.of(BigDecimal.valueOf(500), 1, RiskLevel.LOW, "Amount is within acceptable threshold")
        );
    }

    private PaymentRiskRequest paymentRiskRequest(BigDecimal amount) {
        return PaymentRiskRequest.builder()
                .paymentId("payment-001")
                .customerId("customer-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(amount)
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountryCode("US")
                .buyerIp("127.0.0.1")
                .build();
    }
}
