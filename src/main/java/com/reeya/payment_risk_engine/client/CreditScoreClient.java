package com.reeya.payment_risk_engine.client;

import java.time.LocalDate;
import java.util.OptionalInt;

/**
 * Client contract for retrieving credit scores.
 */
public interface CreditScoreClient {

    OptionalInt calculateCreditScore(String customerId, LocalDate businessDate);
}
