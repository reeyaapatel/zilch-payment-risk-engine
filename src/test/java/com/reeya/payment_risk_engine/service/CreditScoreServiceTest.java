package com.reeya.payment_risk_engine.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.reeya.payment_risk_engine.client.StubCreditScoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;

public class CreditScoreServiceTest {

    private StubCreditScoreClient creditScoreClient;
    private CreditScoreService creditScoreService;

    @BeforeEach
    public void setUp() {
        creditScoreClient = Mockito.mock(StubCreditScoreClient.class);
        creditScoreService = new CreditScoreService(Caffeine.newBuilder().build(), creditScoreClient);
    }

    @Test
    public void getCreditScore_whenCustomerIdAndBusinessDateMatchUsesCachedScore() {
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(601);

        int firstScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);
        int secondScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);

        assertEquals(firstScore, secondScore);
        assertEquals(601, firstScore);
        Mockito.verify(creditScoreClient).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }

    @Test
    public void getCreditScore_whenBusinessDateIsDifferentCalculatesAgain() {
        LocalDate firstBusinessDate = LocalDate.parse("2026-05-30");
        LocalDate secondBusinessDate = LocalDate.parse("2026-05-31");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", firstBusinessDate))
                .thenReturn(601);
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", secondBusinessDate))
                .thenReturn(602);

        int firstScore = creditScoreService.getCreditScore("CUSTOMER-001", firstBusinessDate);
        int secondScore = creditScoreService.getCreditScore("CUSTOMER-001", secondBusinessDate);

        assertEquals(601, firstScore);
        assertEquals(602, secondScore);
        Mockito.verify(creditScoreClient).calculateCreditScore("CUSTOMER-001", firstBusinessDate);
        Mockito.verify(creditScoreClient).calculateCreditScore("CUSTOMER-001", secondBusinessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }

    @Test
    public void getCreditScore_whenCustomerIdIsDifferentCalculatesAgain() {
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(601);
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-002", businessDate))
                .thenReturn(602);

        int firstScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);
        int secondScore = creditScoreService.getCreditScore("CUSTOMER-002", businessDate);

        assertEquals(601, firstScore);
        assertEquals(602, secondScore);
        Mockito.verify(creditScoreClient).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verify(creditScoreClient).calculateCreditScore("CUSTOMER-002", businessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }
}
