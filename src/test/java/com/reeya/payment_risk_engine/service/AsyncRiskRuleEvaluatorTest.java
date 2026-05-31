package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.rules.RiskRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AsyncRiskRuleEvaluatorTest {

    @Mock
    private RiskRule riskRule1;

    @Mock
    private RiskRule riskRule2;

    private PaymentRiskRequest paymentRiskRequest;

    private final Executor directExecutor = Runnable::run;

    @BeforeEach
    public void setUp() {
        paymentRiskRequest = PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .merchantCountryCode("GB")
                .merchantName("ASOS")
                .currency("GBP")
                .buyerIp("1.2.3.4")
                .build();
    }

    @Test
    public void evaluate_whenRulesSucceedReturnsRuleResults() {
        // GIVEN
        AsyncRiskRuleEvaluator evaluator = new AsyncRiskRuleEvaluator(
                List.of(riskRule1, riskRule2),
                directExecutor,
                40,
                3);
        RiskRuleResult amountResult = new RiskRuleResult("AMOUNT_RULE", 1, RiskLevel.LOW, "Low risk");
        RiskRuleResult ipResult = new RiskRuleResult("IP_CHECK", 50, RiskLevel.MEDIUM, "IP mismatch");
        Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(amountResult);
        Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(ipResult);

        // WHEN
        List<RiskRuleResult> results = evaluator.evaluate(paymentRiskRequest);

        // THEN
        assertEquals(List.of(amountResult, ipResult), results);
        Mockito.verify(riskRule1).evaluate(paymentRiskRequest);
        Mockito.verify(riskRule2).evaluate(paymentRiskRequest);
        Mockito.verifyNoMoreInteractions(riskRule1, riskRule2);
    }

    @Test
    public void evaluate_whenRuleFailsUsesHighRiskFallbackResult() {
        // GIVEN
        AsyncRiskRuleEvaluator evaluator = new AsyncRiskRuleEvaluator(
                List.of(riskRule1, riskRule2),
                directExecutor,
                40,
                3);
        RiskRuleResult ipResult = new RiskRuleResult("IP_CHECK", 1, RiskLevel.LOW, "IP matched");
        Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenThrow(new IllegalStateException("Rule failed"));
        Mockito.when(riskRule1.getRuleName()).thenReturn("BROKEN_RULE");
        Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(ipResult);

        // WHEN
        List<RiskRuleResult> results = evaluator.evaluate(paymentRiskRequest);

        // THEN
        assertEquals(List.of(
                new RiskRuleResult("BROKEN_RULE", 40, RiskLevel.HIGH, "Rule failed or timed out"),
                ipResult
        ), results);
        Mockito.verify(riskRule1).evaluate(paymentRiskRequest);
        Mockito.verify(riskRule1).getRuleName();
        Mockito.verify(riskRule2).evaluate(paymentRiskRequest);
        Mockito.verifyNoMoreInteractions(riskRule1, riskRule2);
    }

    @Test
    public void evaluate_whenRuleTimesOutUsesHighRiskFallbackResult() {
        // GIVEN
        Executor blockedExecutor = runnable -> {
        };
        AsyncRiskRuleEvaluator evaluator = new AsyncRiskRuleEvaluator(
                List.of(riskRule1),
                blockedExecutor,
                40,
                1);
        Mockito.when(riskRule1.getRuleName()).thenReturn("SLOW_RULE");

        // WHEN
        List<RiskRuleResult> results = evaluator.evaluate(paymentRiskRequest);

        // THEN
        assertEquals(List.of(
                new RiskRuleResult("SLOW_RULE", 40, RiskLevel.HIGH, "Rule failed or timed out")
        ), results);
        Mockito.verify(riskRule1).getRuleName();
        Mockito.verifyNoMoreInteractions(riskRule1);
    }

    @Test
    public void constructor_whenFallbackScoreIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AsyncRiskRuleEvaluator(List.of(riskRule1), directExecutor, -1, 3)
        );

        // THEN
        assertEquals("Fallback score must be non-negative and timeout must be positive", exception.getMessage());
    }

    @Test
    public void constructor_whenTimeoutIsZeroThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AsyncRiskRuleEvaluator(List.of(riskRule1), directExecutor, 40, 0)
        );

        // THEN
        assertEquals("Fallback score must be non-negative and timeout must be positive", exception.getMessage());
    }

    @Test
    public void constructor_whenRuleListIsEmptyThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AsyncRiskRuleEvaluator(List.of(), directExecutor, 40, 3)
        );

        // THEN
        assertEquals("At least one risk rule must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenRuleListIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AsyncRiskRuleEvaluator(null, directExecutor, 40, 3)
        );

        // THEN
        assertEquals("At least one risk rule must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenExecutorIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AsyncRiskRuleEvaluator(List.of(riskRule1), null, 40, 3)
        );

        // THEN
        assertEquals("Risk rule executor must be provided", exception.getMessage());
    }
}
