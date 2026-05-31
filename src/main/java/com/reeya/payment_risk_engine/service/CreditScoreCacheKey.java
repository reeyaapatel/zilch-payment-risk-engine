package com.reeya.payment_risk_engine.service;

import java.time.LocalDate;

/**
 * Cache key for credit score calculation.
 */
public record CreditScoreCacheKey(String customerId, LocalDate businessDate) {
}
