package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleSumRiskScoreCalculatorTest {

    private final SimpleSumRiskScoreCalculator calculator = new SimpleSumRiskScoreCalculator();

    @Test
    public void calculate_whenResultsProvidedReturnsSumOfScores() {
        // GIVEN
        List<RiskRuleResult> results = List.of(
                ruleResult("AMOUNT_RULE", 10),
                ruleResult("IP_CHECK", 20),
                ruleResult("CREDIT_SCORE", 5)
        );

        // WHEN
        int score = calculator.calculate(results);

        // THEN
        assertEquals(35, score);
    }

    @Test
    public void calculate_whenResultsAreEmptyReturnsZero() {
        // WHEN
        int score = calculator.calculate(List.of());

        // THEN
        assertEquals(0, score);
    }

    @Test
    public void calculate_whenResultsAreNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(null)
        );

        // THEN
        assertEquals("Risk rule results must not be null", exception.getMessage());
    }

    private RiskRuleResult ruleResult(String ruleName, int score) {
        return new RiskRuleResult(ruleName, score, RiskLevel.LOW, "reason");
    }
}
