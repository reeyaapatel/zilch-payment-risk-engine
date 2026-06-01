package com.reeya.payment_risk_engine.service.risk;

import com.reeya.payment_risk_engine.model.risk.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleThresholdRiskDecisionPolicyTest {

    @ParameterizedTest
    @MethodSource("decisionCases")
    public void determineDecision_shouldReturnExpectedStatus(int riskScore, Status expectedStatus) {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(riskScore);

        // THEN
        assertEquals(expectedStatus, status);
    }

    @Test
    public void constructor_whenDeclineThresholdIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleThresholdRiskDecisionPolicy(-1, 40)
        );

        // THEN
        assertEquals("Thresholds must be non-negative", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleThresholdRiskDecisionPolicy(70, -1)
        );

        // THEN
        assertEquals("Thresholds must be non-negative", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdEqualsDeclineThresholdThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleThresholdRiskDecisionPolicy(70, 70)
        );

        // THEN
        assertEquals("Review threshold must be lower than decline threshold", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdIsGreaterThanDeclineThresholdThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleThresholdRiskDecisionPolicy(70, 80)
        );

        // THEN
        assertEquals("Review threshold must be lower than decline threshold", exception.getMessage());
    }

    private static Stream<Arguments> decisionCases() {
        return Stream.of(
                Arguments.of(101, Status.DECLINED),
                Arguments.of(70, Status.DECLINED),
                Arguments.of(43, Status.REQUIRES_REVIEW),
                Arguments.of(40, Status.REQUIRES_REVIEW),
                Arguments.of(11, Status.APPROVED)
        );
    }
}
