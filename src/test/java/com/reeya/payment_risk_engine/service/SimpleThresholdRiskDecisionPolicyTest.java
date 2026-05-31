package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleThresholdRiskDecisionPolicyTest {

    @Test
    public void determineDecision_whenRiskScoreAboveDeclineThresholdReturnsDeclined() {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(101);

        // THEN
        assertEquals(Status.DECLINED, status);
    }

    @Test
    public void determineDecision_whenRiskScoreEqualsDeclineThresholdReturnsDeclined() {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(70);

        // THEN
        assertEquals(Status.DECLINED, status);
    }

    @Test
    public void determineDecision_whenRiskScoreIsWithinReviewThresholdReturnsRequiresReview() {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(43);

        // THEN
        assertEquals(Status.REQUIRES_REVIEW, status);
    }

    @Test
    public void determineDecision_whenRiskScoreEqualsReviewThresholdReturnsRequiresReview() {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(40);

        // THEN
        assertEquals(Status.REQUIRES_REVIEW, status);
    }

    @Test
    public void determineDecision_whenRiskScoreIsBelowReviewThresholdReturnsApproved() {
        // GIVEN
        SimpleThresholdRiskDecisionPolicy policy = new SimpleThresholdRiskDecisionPolicy(70, 40);

        // WHEN
        Status status = policy.determineDecision(11);

        // THEN
        assertEquals(Status.APPROVED, status);
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
}
