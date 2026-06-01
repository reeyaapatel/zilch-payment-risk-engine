package com.reeya.payment_risk_engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.reeya.payment_risk_engine.service.credit.CreditScoreCacheKey;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CreditScoreCacheConfigTest {

    private final CreditScoreCacheConfig config = new CreditScoreCacheConfig();

    @Test
    public void creditScoreCache_whenConfigurationIsValidCreatesCache() {
        // WHEN
        Cache<CreditScoreCacheKey, Integer> cache = config.creditScoreCache(100, 15);

        // THEN
        assertEquals(100, cache.policy().eviction().orElseThrow().getMaximum());
        assertEquals(15, cache.policy().expireAfterWrite().orElseThrow().getExpiresAfter(TimeUnit.MINUTES));
    }

    @Test
    public void creditScoreCache_whenMaximumSizeIsNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> config.creditScoreCache(0, 15)
        );

        // THEN
        assertEquals(expectedErrorMessage(), exception.getMessage());
    }

    @Test
    public void creditScoreCache_whenExpirationIsNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> config.creditScoreCache(100, 0)
        );

        // THEN
        assertEquals(expectedErrorMessage(), exception.getMessage());
    }

    private String expectedErrorMessage() {
        return "Invalid Cache configuration: must have positive maximum size and expiration time";
    }
}
