package com.reeya.payment_risk_engine.client;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditScoreClientTest {

    @Test
    public void calculateCreditScore_returnsScoreInExpectedRange()
    {
        //GIVEN
        CreditScoreClient creditScoreClient = new CreditScoreClient();

        //WHEN
        int score = creditScoreClient.calculateCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30"));

        //THEN
        assertTrue(score >= 300);
        assertTrue(score <= 900);
    }
}
