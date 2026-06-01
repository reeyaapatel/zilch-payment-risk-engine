package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleSumRiskScoreCalculatorTest {

    private final SimpleSumRiskScoreCalculator calculator = new SimpleSumRiskScoreCalculator();

    @ParameterizedTest
    @MethodSource("scoreCases")
    public void calculate_shouldReturnExpectedScore(List<RiskRuleResult> results, int expectedScore) {
        // WHEN
        int score = calculator.calculate(results);

        // THEN
        assertEquals(expectedScore, score);
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

    private static Stream<Arguments> scoreCases() {
        return Stream.of(
                Arguments.of(List.of(), 0),
                Arguments.of(List.of(ruleResult("AMOUNT_RULE", 10)), 10),
                Arguments.of(List.of(
                        ruleResult("AMOUNT_RULE", 10),
                        ruleResult("IP_CHECK", 20),
                        ruleResult("CREDIT_SCORE", 5)
                ), 35)
        );
    }

    private static RiskRuleResult ruleResult(String ruleName, int score) {
        return new RiskRuleResult(ruleName, score, RiskLevel.LOW, "reason");
    }
}
