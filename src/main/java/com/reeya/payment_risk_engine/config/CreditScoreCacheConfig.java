package com.reeya.payment_risk_engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.reeya.payment_risk_engine.service.CreditScoreCacheKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Credit score cache configuration - in reality this would be a distributed cache like Redis as services
 * are likely to horizonally scaled. The mock global cache is in place for credit score calculations as this can take a long time
 * so caching the results can prevent repeated calls to the credit score service -> improving latency
 */
@Configuration
public class CreditScoreCacheConfig {

    @Bean
    public Cache<CreditScoreCacheKey, Integer> creditScoreCache(
            @Value("${credit.score.cache.maximum-size:10000}") long maximumSize,
            @Value("${credit.score.cache.expire-after-write-minutes:1000}") long expireAfterWriteMinutes)
    {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(expireAfterWriteMinutes))
                .build();
    }
}
