package com.reeya.payment_risk_engine.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a mock client to simulate the credit score retrieval for a customer id.
 */
@Component
public class StubCreditScoreClient {

    public int calculateCreditScore(String customerId, LocalDate businessDate)
    {
        return ThreadLocalRandom.current().nextInt(300, 901);
    }
}
