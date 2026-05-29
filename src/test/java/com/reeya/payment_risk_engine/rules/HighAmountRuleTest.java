package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HighAmountRuleTest {

    private HighAmountRule highAmountRule;

    @BeforeEach
    void setUp() {
        highAmountRule = new HighAmountRule();
    }

    @Test
    void evaluate_shouldReturnHighRisk_whenAmountIsGreaterThan1000() {
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("payment-001")
                .amount(BigDecimal.valueOf(1000.01))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountry("US")
                .buyerIp("127.0.0.1")
                .build();

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(10, result.getScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertEquals("Amount exceeds threshold of 1000", result.getReason());
    }

    @Test
    void evaluate_shouldReturnMediumRisk_whenAmountIsGreaterThan500AndLessThanOrEqualTo1000() {
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("payment-002")
                .amount(BigDecimal.valueOf(750))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountry("US")
                .buyerIp("127.0.0.1")
                .build();

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(5, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertEquals("Amount exceeds threshold of 500", result.getReason());
    }

    @Test
    void evaluate_shouldReturnLowRisk_whenAmountIsLessThanOrEqualTo500() {
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("payment-003")
                .amount(BigDecimal.valueOf(500))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountry("US")
                .buyerIp("127.0.0.1")
                .build();

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(1, result.getScore());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertEquals("Amount is within acceptable threshold", result.getReason());
    }

    @Test
    void evaluate_shouldReturnMediumRisk_whenAmountIsExactlyAbove500() {
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("payment-004")
                .amount(BigDecimal.valueOf(500.01))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountry("US")
                .buyerIp("127.0.0.1")
                .build();

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(5, result.getScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertEquals("Amount exceeds threshold of 500", result.getReason());
    }

    @Test
    void evaluate_shouldReturnHighRisk_whenAmountIsExactlyAbove1000() {
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("payment-005")
                .amount(BigDecimal.valueOf(1000.01))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCountry("US")
                .buyerIp("127.0.0.1")
                .build();

        RiskRuleResult result = highAmountRule.evaluate(request);

        assertEquals("HIGH_AMOUNT_RULE", result.getRuleName());
        assertEquals(10, result.getScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertEquals("Amount exceeds threshold of 1000", result.getReason());
    }
}
