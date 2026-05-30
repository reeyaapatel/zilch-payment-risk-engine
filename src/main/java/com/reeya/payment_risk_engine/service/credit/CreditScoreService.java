package com.reeya.payment_risk_engine.service.credit;

import com.github.benmanes.caffeine.cache.Cache;
import com.reeya.payment_risk_engine.client.CreditScoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service for calculating and caching credit scores.
 */
@Service
public class CreditScoreService {

    private final Cache<CreditScoreCacheKey, Integer> creditScoreCache;
    private final CreditScoreClient creditScoreClient;

    public CreditScoreService(Cache<CreditScoreCacheKey, Integer> creditScoreCache, CreditScoreClient creditScoreClient)
    {
        this.creditScoreCache = creditScoreCache;
        this.creditScoreClient = creditScoreClient;
    }

    public int getCreditScore(String customerId, LocalDate businessDate) {
        return creditScoreCache.get(
                new CreditScoreCacheKey(customerId, businessDate),
                cacheKey -> creditScoreClient.calculateCreditScore(cacheKey.customerId(), cacheKey.businessDate())
        );
    }
}
