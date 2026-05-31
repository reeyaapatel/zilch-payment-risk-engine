package com.reeya.payment_risk_engine.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.reeya.payment_risk_engine.client.StubCreditScoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        //GIVEN
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(OptionalInt.of(601));
        //WHEN
        OptionalInt firstScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);
        OptionalInt secondScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);

        //THEN
        assertTrue(firstScore.isPresent());
        assertEquals(601, firstScore.getAsInt());
        assertEquals(firstScore, secondScore);
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }

    @Test
    public void getCreditScore_whenBusinessDateIsDifferentCalculatesAgain() {
        //GIVEN
        LocalDate firstBusinessDate = LocalDate.parse("2026-05-30");
        LocalDate secondBusinessDate = LocalDate.parse("2026-05-31");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", firstBusinessDate))
                .thenReturn(OptionalInt.of(601));
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", secondBusinessDate))
                .thenReturn(OptionalInt.of(602));

        //WHEN
        OptionalInt firstScore = creditScoreService.getCreditScore("CUSTOMER-001", firstBusinessDate);
        OptionalInt secondScore = creditScoreService.getCreditScore("CUSTOMER-001", secondBusinessDate);

        //THEN
        assertTrue(firstScore.isPresent());
        assertTrue(secondScore.isPresent());
        assertEquals(601, firstScore.getAsInt());
        assertEquals(602, secondScore.getAsInt());
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-001", firstBusinessDate);
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-001", secondBusinessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }

    @Test
    public void getCreditScore_whenCustomerIdIsDifferentCalculatesAgain() {
        //GIVEM
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(OptionalInt.of(601));
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-002", businessDate))
                .thenReturn(OptionalInt.of(602));

        //WHEN
        OptionalInt firstScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);
        OptionalInt secondScore = creditScoreService.getCreditScore("CUSTOMER-002", businessDate);

        //THEN
        assertTrue(firstScore.isPresent());
        assertTrue(secondScore.isPresent());
        assertEquals(601, firstScore.getAsInt());
        assertEquals(602, secondScore.getAsInt());
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verify(creditScoreClient, times(1)).calculateCreditScore("CUSTOMER-002", businessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }

    @Test
    public void getCreditScore_whenScoreIsMissingDoesNotCacheMissingScore() {
        //GIVEN
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(OptionalInt.empty())
                .thenReturn(OptionalInt.of(601));

        //WHEN
        OptionalInt firstScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);
        OptionalInt secondScore = creditScoreService.getCreditScore("CUSTOMER-001", businessDate);

        //THEN
        assertTrue(firstScore.isEmpty());
        assertTrue(secondScore.isPresent());
        assertEquals(601, secondScore.getAsInt());
        Mockito.verify(creditScoreClient, times(2)).calculateCreditScore("CUSTOMER-001", businessDate);
        Mockito.verifyNoMoreInteractions(creditScoreClient);
    }
}
