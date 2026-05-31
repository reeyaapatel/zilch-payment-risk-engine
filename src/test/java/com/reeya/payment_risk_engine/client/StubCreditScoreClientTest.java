package com.reeya.payment_risk_engine.client;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StubCreditScoreClientTest {

    @Test
    public void calculateCreditScore_returnsScoreInExpectedRange() {
        // GIVEN
        StubCreditScoreClient creditScoreClient = new StubCreditScoreClient();

        // WHEN
        OptionalInt score = creditScoreClient.calculateCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30"));

        // THEN
        assertTrue(score.isPresent());
        assertTrue(score.getAsInt() >= 300);
        assertTrue(score.getAsInt() <= 900);
    }
}
