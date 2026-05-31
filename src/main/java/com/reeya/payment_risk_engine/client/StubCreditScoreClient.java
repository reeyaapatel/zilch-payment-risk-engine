package com.reeya.payment_risk_engine.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a mock client to simulate the credit score retrieval for a customer id.
 */
@Component
public class StubCreditScoreClient {

    /**
     * Simulates a credit score calculation for a customer id. - so not currently using input parameters
     * @param customerId
     * @param businessDate
     * @return OptionalInt
     */
    public OptionalInt calculateCreditScore(String customerId, LocalDate businessDate)
    {
        return OptionalInt.of(ThreadLocalRandom.current().nextInt(300, 901));
    }
}
