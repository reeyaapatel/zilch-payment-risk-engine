
package com.reeya.payment_risk_engine.service;


import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
import com.reeya.payment_risk_engine.rules.RiskRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PaymentRiskServiceTest {


    @Mock
    private EntityManager entityManager;

    @Mock
    private RiskRule riskRule1;

    @Mock
    private RiskRule riskRule2;

    private PaymentRiskService paymentRiskService;

    private PaymentRiskRequest paymentRiskRequest;

    private final List<String> expectedReasons = List.of("Low risk", "IP mismatch");
    private final Executor directExecutor = Runnable::run;

    @BeforeEach
    public void setUp() {

        paymentRiskService = new PaymentRiskService(entityManager, Arrays.asList(riskRule1, riskRule2), directExecutor, 70, 40, 3);
        paymentRiskRequest = PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(new BigDecimal(100))
                .merchantCountryCode("UK")
                .merchantName("MARKS&SPENCER")
                .currency("GBP")
                .buyerIp("1.2.3.4")
                .build();

        }

        @Test
        public void assessRisk_whenRiskScoreAboveHighRiskThreshold() {
            //GIVEN
            mockRules(1, 100, RiskLevel.HIGH);

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            PaymentRisk savedPayment = verifySavedPayment();
            assertSavedPayment(savedPayment, 101, Status.DECLINED);
            assertResponse(response, 101, Status.DECLINED);
            verifyPaymentLookup();
            verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsEqualToHighThreshold() {
        //GIVEN
        mockRules(1, 69, RiskLevel.HIGH);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 70, Status.DECLINED);
        assertResponse(response, 70, Status.DECLINED);
        verifyPaymentLookup();
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsWithinMediumThreshold() {
        //GIVEN
        mockRules(1, 42, RiskLevel.HIGH);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 43, Status.REQUIRES_REVIEW);
        assertResponse(response, 43, Status.REQUIRES_REVIEW);
        verifyPaymentLookup();
        verifyRuleCalls();
    }
        @Test
        public void assessRisk_whenRiskScoreIsEqualToMediumThreshold() {
            //GIVEN
            mockRules(1, 39, RiskLevel.HIGH);

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            PaymentRisk savedPayment = verifySavedPayment();
            assertSavedPayment(savedPayment, 40, Status.REQUIRES_REVIEW);
            assertResponse(response, 40, Status.REQUIRES_REVIEW);
            verifyPaymentLookup();
            verifyRuleCalls();
    }


    @Test
    public void assessRisk_whenRiskScoreIsWithinLowThreshold() {
        //GIVEN
        mockRules(1, 10, RiskLevel.LOW);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 11, Status.APPROVED);
        assertResponse(response, 11, Status.APPROVED);
        verifyPaymentLookup();
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenPaymentAlreadyExistsReturnsExistingPayment() {
        //GIVEN
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        assertStoredPaymentResponse(response, 40, Status.REQUIRES_REVIEW);
        verifyPaymentLookup();
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }


    @Test
    public void assessRisk_whenPersistFlushFailsFetchesExistingPayment() {
        //GIVEN
        mockRules(1, 39, RiskLevel.HIGH);
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        Mockito.doThrow(new PersistenceException("Duplicate payment"))
                .when(entityManager)
                .flush();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(null, storedPayment);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 40, Status.REQUIRES_REVIEW);
        assertStoredPaymentResponse(response, 40, Status.REQUIRES_REVIEW);
        Mockito.verify(entityManager).flush();
        Mockito.verify(entityManager, Mockito.times(2)).find(PaymentRisk.class, "PAY-001");
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenPersistFlushFailsAndPaymentDoesNotExistThrowsError() {
        //GIVEN
        mockRules(1, 39, RiskLevel.HIGH);
        Mockito.doThrow(new PersistenceException("Duplicate payment"))
                .when(entityManager)
                .flush();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001"))
                .thenReturn(null)
                .thenReturn(null);

        //WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.assessRisk(paymentRiskRequest)
        );

        //THEN
        assertEquals("Payment not found: PAY-001", exception.getMessage());
        verifySavedPayment();
        Mockito.verify(entityManager).flush();
        Mockito.verify(entityManager, Mockito.times(2)).find(PaymentRisk.class, "PAY-001");
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRuleFailsUsesHighRiskFallbackResult() {
        //GIVEN
        Mockito.when(riskRule1.evaluate(paymentRiskRequest))
                .thenThrow(new IllegalStateException("Rule failed"));
        Mockito.when(riskRule1.getRuleName()).thenReturn("BROKEN_RULE");
        Mockito.when(riskRule2.evaluate(paymentRiskRequest))
                .thenReturn(ruleResult("IP_CHECK", 1, RiskLevel.LOW, "IP matched"));

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertEquals(41, savedPayment.getRiskScore());
        assertEquals(Status.REQUIRES_REVIEW, savedPayment.getStatus());
        assertEquals(List.of("Rule failed or timed out", "IP matched"), savedPayment.getReasons());
        assertEquals(41, response.getRiskScore());
        assertEquals(Status.REQUIRES_REVIEW, response.getStatus());
        assertEquals(List.of("Rule failed or timed out", "IP matched"), response.getReasons());
        verifyPaymentLookup();
        Mockito.verify(riskRule1).evaluate(paymentRiskRequest);
        Mockito.verify(riskRule1).getRuleName();
        Mockito.verify(riskRule2).evaluate(paymentRiskRequest);
        Mockito.verify(entityManager).flush();
        Mockito.verifyNoMoreInteractions(entityManager, riskRule1, riskRule2);
    }

    @Test
    public void getPaymentRiskResponse_whenPaymentDoesNotExistThrowsError() {
        //GIVEN
        Mockito.when(entityManager.find(PaymentRisk.class, "missing-id")).thenReturn(null);

        //WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.getPaymentRiskResponse("missing-id")
        );

        //THEN
        assertEquals("Payment not found: missing-id", exception.getMessage());
        Mockito.verify(entityManager).find(PaymentRisk.class, "missing-id");
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentRequiresReviewUpdatesStatus() {
        //GIVEN
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
                .status(Status.APPROVED)
                .build();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.updateStatus("PAY-001", update);

        //THEN
        assertEquals(Status.APPROVED, storedPayment.getStatus());
        assertTrue(storedPayment.getLastUpdatedAt().isAfter(Instant.parse("2026-05-29T10:15:30Z")));
        assertEquals("PAY-001", response.getPaymentId());
        assertEquals(1, response.getVersion());
        assertEquals(40, response.getRiskScore());
        assertEquals(Status.APPROVED, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertEquals(Instant.parse("2026-05-29T10:15:30Z"), response.getCreatedAt());
        assertEquals(storedPayment.getLastUpdatedAt(), response.getLastUpdatedAt());
        Mockito.verify(entityManager).find(PaymentRisk.class, "PAY-001");
        Mockito.verify(entityManager).flush();
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentDoesNotRequireReviewThrowsError() {
        //GIVEN
        PaymentRisk storedPayment = paymentRisk(11, Status.APPROVED);
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
                .status(Status.DECLINED)
                .build();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        //WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentRiskService.updateStatus("PAY-001", update)
        );

        //THEN
        assertEquals("Payment status can only be updated when it requires review", exception.getMessage());
        assertEquals(Status.APPROVED, storedPayment.getStatus());
        Mockito.verify(entityManager).find(PaymentRisk.class, "PAY-001");
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentIsDeclinedThrowsError() {
        //GIVEN
        PaymentRisk storedPayment = paymentRisk(70, Status.DECLINED);
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
                .status(Status.APPROVED)
                .build();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        //WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentRiskService.updateStatus("PAY-001", update)
        );

        //THEN
        assertEquals("Payment status can only be updated when it requires review", exception.getMessage());
        assertEquals(Status.DECLINED, storedPayment.getStatus());
        Mockito.verify(entityManager).find(PaymentRisk.class, "PAY-001");
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentDoesNotExistThrowsError() {
        //GIVEN
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
                .status(Status.APPROVED)
                .build();
        Mockito.when(entityManager.find(PaymentRisk.class, "doesnt-exist-id")).thenReturn(null);

        //WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.updateStatus("doesnt-exist-id", update)
        );

        //THEN
        assertEquals("Payment not found: doesnt-exist-id", exception.getMessage());
        Mockito.verify(entityManager).find(PaymentRisk.class, "doesnt-exist-id");
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentIdIsNullThrowsError() {
        //GIVEN
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
                .status(Status.APPROVED)
                .build();

        //WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.updateStatus(null, update)
        );

        //THEN
        assertEquals("Payment id is required", exception.getMessage());
        Mockito.verifyNoInteractions(entityManager, riskRule1, riskRule2);
    }

    private void mockRules(int firstScore, int secondScore, RiskLevel secondRiskLevel) {
        Mockito.when(riskRule1.evaluate(paymentRiskRequest))
                .thenReturn(ruleResult("HIGH_AMOUNT", firstScore, RiskLevel.LOW, "Low risk"));
        Mockito.when(riskRule2.evaluate(paymentRiskRequest))
                .thenReturn(ruleResult("IP_CHECK", secondScore, secondRiskLevel, "IP mismatch"));
    }


    private PaymentRisk verifySavedPayment() {
        ArgumentCaptor<PaymentRisk> captor = ArgumentCaptor.forClass(PaymentRisk.class);
        Mockito.verify(entityManager).persist(captor.capture());
        return captor.getValue();
    }

    private void verifyPaymentLookup() {
        Mockito.verify(entityManager).find(PaymentRisk.class, "PAY-001");
    }

    private void verifyRuleCalls() {
        Mockito.verify(riskRule1).evaluate(paymentRiskRequest);
        Mockito.verify(riskRule2).evaluate(paymentRiskRequest);
        Mockito.verify(entityManager).flush();
        Mockito.verifyNoMoreInteractions(entityManager, riskRule1, riskRule2);
    }

    private void assertSavedPayment(PaymentRisk savedPayment, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", savedPayment.getPaymentId());
        assertEquals(1, savedPayment.getVersion());
        assertEquals("CUSTOMER-001", savedPayment.getCustomerId());
        assertEquals(LocalDate.parse("2026-05-30"), savedPayment.getBusinessDate());
        assertEquals(BigDecimal.valueOf(100), savedPayment.getAmount());
        assertEquals("GBP", savedPayment.getCurrency());
        assertEquals("MARKS&SPENCER", savedPayment.getMerchantName());
        assertEquals("UK", savedPayment.getMerchantCountryCode());
        assertEquals("1.2.3.4", savedPayment.getBuyerIp());
        assertEquals(expectedRiskScore, savedPayment.getRiskScore());
        assertEquals(expectedStatus, savedPayment.getStatus());
        assertEquals(expectedReasons, savedPayment.getReasons());
        assertNotNull(savedPayment.getCreatedAt());
        assertNotNull(savedPayment.getLastUpdatedAt());
    }

    private void assertResponse(PaymentRiskResponse response, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", response.getPaymentId());
        assertEquals(1, response.getVersion());
        assertEquals("CUSTOMER-001", response.getCustomerId());
        assertEquals(LocalDate.parse("2026-05-30"), response.getBusinessDate());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals("GBP", response.getCurrency());
        assertEquals("MARKS&SPENCER", response.getMerchantName());
        assertEquals("UK", response.getMerchantCountryCode());
        assertEquals("1.2.3.4", response.getBuyerIp());
        assertEquals(expectedRiskScore, response.getRiskScore());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getLastUpdatedAt());
    }

    private void assertStoredPaymentResponse(PaymentRiskResponse response, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", response.getPaymentId());
        assertEquals(1, response.getVersion());
        assertEquals("CUSTOMER-001", response.getCustomerId());
        assertEquals(expectedRiskScore, response.getRiskScore());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertEquals(LocalDate.parse("2026-05-30"), response.getBusinessDate());
        assertEquals(Instant.parse("2026-05-29T10:15:30Z"), response.getCreatedAt());
        assertEquals(Instant.parse("2026-05-29T10:15:30Z"), response.getLastUpdatedAt());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals("GBP", response.getCurrency());
        assertEquals("MARKS&SPENCER", response.getMerchantName());
        assertEquals("UK", response.getMerchantCountryCode());
        assertEquals("1.2.3.4", response.getBuyerIp());
    }

    private PaymentRisk paymentRisk(int riskScore, Status status) {
        return PaymentRisk.builder()
                .paymentId("PAY-001")
                .version(1)
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("MARKS&SPENCER")
                .merchantCountryCode("UK")
                .buyerIp("1.2.3.4")
                .riskScore(riskScore)
                .status(status)
                .reasons(expectedReasons)
                .createdAt(Instant.parse("2026-05-29T10:15:30Z"))
                .lastUpdatedAt(Instant.parse("2026-05-29T10:15:30Z"))
                .build();
    }

    private RiskRuleResult ruleResult(String ruleName, int score, RiskLevel riskLevel, String reason) {
        return RiskRuleResult.builder()
                .ruleName(ruleName)
                .score(score)
                .riskLevel(riskLevel)
                .reason(reason)
                .build();
    }

}
