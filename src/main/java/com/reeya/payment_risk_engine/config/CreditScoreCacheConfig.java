package com.reeya.payment_risk_engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.reeya.payment_risk_engine.service.credit.CreditScoreCacheKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Credit score cache configuration.
 */
@Configuration
public class CreditScoreCacheConfig {

    @Bean
    public Cache<CreditScoreCacheKey, Integer> creditScoreCache(
            @Value("${credit.score.cache.maximum-size}") long maximumSize,
            @Value("${credit.score.cache.expire-after-write-minutes}") long expireAfterWriteMinutes) {
        if (maximumSize <= 0 || expireAfterWriteMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Cache configuration: must have positive maximum size and expiration time");
        }
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(expireAfterWriteMinutes))
                .build();
    }
}
