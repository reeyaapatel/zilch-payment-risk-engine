package com.reeya.payment_risk_engine.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.reeya.payment_risk_engine.client.StubCreditScoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.OptionalInt;

/**
 * Service for calculating and caching credit scores.
 */
@Service
public class CreditScoreService {

    private final Cache<CreditScoreCacheKey, Integer> creditScoreCache;
    private final StubCreditScoreClient creditScoreClient;

    public CreditScoreService(Cache<CreditScoreCacheKey, Integer> creditScoreCache, StubCreditScoreClient creditScoreClient)
    {
        this.creditScoreCache = creditScoreCache;
        this.creditScoreClient = creditScoreClient;
    }

    public OptionalInt getCreditScore(String customerId, LocalDate businessDate)
    {
        Integer cachedScore = creditScoreCache.getIfPresent(new CreditScoreCacheKey(customerId, businessDate));
        if (cachedScore != null)
        {
            return OptionalInt.of(cachedScore);
        }

        OptionalInt score = creditScoreClient.calculateCreditScore(customerId, businessDate);
        score.ifPresent(value ->
                creditScoreCache.put(new CreditScoreCacheKey(customerId, businessDate), value)
        );
        return score;
    }
}
