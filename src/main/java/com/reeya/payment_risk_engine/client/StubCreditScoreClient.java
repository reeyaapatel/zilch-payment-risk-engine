package com.reeya.payment_risk_engine.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stub client to simulate credit score retrieval for a customer id.
 */
@Component
public class StubCreditScoreClient {

    /**
     * Simulates a credit score calculation for a customer id.
     */
    public OptionalInt calculateCreditScore(String customerId, LocalDate businessDate) {
        return OptionalInt.of(ThreadLocalRandom.current().nextInt(300, 901));
    }
}
