package com.reeya.payment_risk_engine.service;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PaymentRiskServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private RiskRuleEvaluator riskRuleEvaluator;

    private PaymentRiskService paymentRiskService;

    private PaymentRiskRequest paymentRiskRequest;

    private final List<String> expectedReasons = List.of("Low risk", "IP mismatch");
    
    @BeforeEach
    public void setUp() {
        paymentRiskService = new PaymentRiskService(
                entityManager,
                riskRuleEvaluator,
                70,
                40);
        paymentRiskRequest = PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .merchantCountryCode("UK")
                .merchantName("MARKS&SPENCER")
                .currency("GBP")
                .buyerIp("1.2.3.4")
                .build();
    }

    @Test
    public void assessRisk_whenRiskScoreAboveHighRiskThreshold() {
        // GIVEN
        mockRules(100, RiskLevel.HIGH);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 101, Status.DECLINED);
        assertResponse(response, 101, Status.DECLINED);
        verifyPaymentLookup(1);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsEqualToHighThreshold() {
        // GIVEN
        mockRules(69, RiskLevel.HIGH);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 70, Status.DECLINED);
        assertResponse(response, 70, Status.DECLINED);
        verifyPaymentLookup(1);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsWithinMediumThreshold() {
        // GIVEN
        mockRules(42, RiskLevel.HIGH);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 43, Status.REQUIRES_REVIEW);
        assertResponse(response, 43, Status.REQUIRES_REVIEW);
        verifyPaymentLookup(1);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsEqualToMediumThreshold() {
        // GIVEN
        mockRules(39, RiskLevel.HIGH);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 40, Status.REQUIRES_REVIEW);
        assertResponse(response, 40, Status.REQUIRES_REVIEW);
        verifyPaymentLookup(1);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsWithinLowThreshold() {
        // GIVEN
        mockRules(10, RiskLevel.LOW);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 11, Status.APPROVED);
        assertResponse(response, 11, Status.APPROVED);
        verifyPaymentLookup(1);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenPaymentAlreadyExistsReturnsExistingPayment() {
        // GIVEN
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        assertStoredPaymentResponse(response, 40, Status.REQUIRES_REVIEW);
        verifyPaymentLookup(1);
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }


    @Test
    public void assessRisk_whenPersistFlushFailsFetchesExistingPayment() {
        // GIVEN
        mockRules(39, RiskLevel.HIGH);
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        Mockito.doThrow(new PersistenceException("Duplicate payment"))
                .when(entityManager)
                .flush();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(null, storedPayment);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();
        assertSavedPayment(savedPayment, 40, Status.REQUIRES_REVIEW);
        assertStoredPaymentResponse(response, 40, Status.REQUIRES_REVIEW);
        Mockito.verify(entityManager, times(1)).flush();
        Mockito.verify(entityManager, times(2)).find(PaymentRisk.class, "PAY-001");
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenPersistFlushFailsAndPaymentDoesNotExistThrowsError() {
        // GIVEN
        mockRules(39, RiskLevel.HIGH);
        Mockito.doThrow(new PersistenceException("Duplicate payment"))
                .when(entityManager)
                .flush();
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001"))
                .thenReturn(null)
                .thenReturn(null);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.assessRisk(paymentRiskRequest)
        );

        // THEN
        assertEquals("Payment not found: PAY-001", exception.getMessage());
        getSavedPayment();
        Mockito.verify(entityManager, times(1)).flush();
        Mockito.verify(entityManager, times(2)).find(PaymentRisk.class, "PAY-001");
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenEvaluatorReturnsFallbackResultSavesFallbackRisk() {
        // GIVEN
        Mockito.when(riskRuleEvaluator.evaluate(paymentRiskRequest))
                .thenReturn(List.of(
                        ruleResult("BROKEN_RULE", 40, RiskLevel.HIGH, "Rule failed or timed out"),
                        ruleResult("IP_CHECK", 1, RiskLevel.LOW, "IP matched")
                ));

        // WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        // THEN
        PaymentRisk savedPayment = getSavedPayment();

        // validate the saved payment
        assertEquals(41, savedPayment.getRiskScore());
        assertEquals(Status.REQUIRES_REVIEW, savedPayment.getStatus());
        assertEquals(List.of("Rule failed or timed out", "IP matched"), savedPayment.getReasons());

        //validate the response
        assertEquals(41, response.getRiskScore());
        assertEquals(Status.REQUIRES_REVIEW, response.getStatus());
        assertEquals(List.of("Rule failed or timed out", "IP matched"), response.getReasons());


        verifyPaymentLookup(1);
        Mockito.verify(riskRuleEvaluator, times(1)).evaluate(paymentRiskRequest);
        Mockito.verify(entityManager, times(1)).flush();
        Mockito.verifyNoMoreInteractions(entityManager, riskRuleEvaluator);
    }

    @Test
    public void getPaymentRiskResponse_whenPaymentDoesNotExistThrowsError() {
        // GIVEN
        Mockito.when(entityManager.find(PaymentRisk.class, "missing-id")).thenReturn(null);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.getPaymentRiskResponse("missing-id")
        );

        // THEN
        assertEquals("Payment not found: missing-id", exception.getMessage());
        Mockito.verify(entityManager, times(1)).find(PaymentRisk.class, "missing-id");
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentRequiresReviewUpdatesStatus() {
        // GIVEN
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        // WHEN
        PaymentRiskResponse response = paymentRiskService.updateStatus("PAY-001", update);

        // THEN
        assertEquals(Status.APPROVED, storedPayment.getStatus());
        assertTrue(storedPayment.getLastUpdatedAt().isAfter(Instant.parse("2026-05-29T10:15:30Z")));
        assertEquals("PAY-001", response.getPaymentId());
        assertEquals(1, response.getVersion());
        assertEquals(40, response.getRiskScore());
        assertEquals(Status.APPROVED, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertEquals(Instant.parse("2026-05-29T10:15:30Z"), response.getCreatedAt());
        assertEquals(storedPayment.getLastUpdatedAt(), response.getLastUpdatedAt());
        Mockito.verify(entityManager, times(1)).find(PaymentRisk.class, "PAY-001");
        Mockito.verify(entityManager, times(1)).flush();
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentDoesNotRequireReviewThrowsError() {
        // GIVEN
        PaymentRisk storedPayment = paymentRisk(11, Status.APPROVED);
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.DECLINED);
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        // WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentRiskService.updateStatus("PAY-001", update)
        );

        // THEN
        assertEquals("Payment status can only be updated when it requires review", exception.getMessage());
        assertEquals(Status.APPROVED, storedPayment.getStatus());
        Mockito.verify(entityManager, times(1)).find(PaymentRisk.class, "PAY-001");
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentIsDeclinedThrowsError() {
        // GIVEN
        PaymentRisk storedPayment = paymentRisk(70, Status.DECLINED);
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);
        Mockito.when(entityManager.find(PaymentRisk.class, "PAY-001")).thenReturn(storedPayment);

        // WHEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> paymentRiskService.updateStatus("PAY-001", update)
        );

        // THEN
        assertEquals("Payment status can only be updated when it requires review", exception.getMessage());
        assertEquals(Status.DECLINED, storedPayment.getStatus());
        Mockito.verify(entityManager, times(1)).find(PaymentRisk.class, "PAY-001");
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentDoesNotExistThrowsError() {
        // GIVEN
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);
        Mockito.when(entityManager.find(PaymentRisk.class, "doesnt-exist-id")).thenReturn(null);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.updateStatus("doesnt-exist-id", update)
        );

        // THEN
        assertEquals("Payment not found: doesnt-exist-id", exception.getMessage());
        Mockito.verify(entityManager).find(PaymentRisk.class, "doesnt-exist-id");
        Mockito.verifyNoInteractions(riskRuleEvaluator);
        Mockito.verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void updateStatus_whenPaymentIdIsNullThrowsError() {
        // GIVEN
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);

        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> paymentRiskService.updateStatus(null, update)
        );

        // THEN
        assertEquals("Payment id is required", exception.getMessage());
        Mockito.verifyNoInteractions(entityManager, riskRuleEvaluator);
    }

    @Test
    public void constructor_whenEntityManagerIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(null, riskRuleEvaluator, 70, 40)
        );

        // THEN
        assertEquals("EntityManager must not be null", exception.getMessage());
    }

    @Test
    public void constructor_whenRiskRuleEvaluatorIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(entityManager, null, 70, 40)
        );

        // THEN
        assertEquals("RiskRuleEvaluator must not be null", exception.getMessage());
    }

    @Test
    public void constructor_whenDeclineThresholdIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(entityManager, riskRuleEvaluator, -1, 40)
        );

        // THEN
        assertEquals("Thresholds must be non-negative", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(entityManager, riskRuleEvaluator, 70, -1)
        );

        // THEN
        assertEquals("Thresholds must be non-negative", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdIsEqualToDeclineThresholdThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(entityManager, riskRuleEvaluator, 70, 70)
        );

        // THEN
        assertEquals("Review threshold must be lower than decline threshold", exception.getMessage());
    }

    @Test
    public void constructor_whenReviewThresholdIsGreaterThanDeclineThresholdThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRiskService(entityManager, riskRuleEvaluator, 70, 80)
        );

        // THEN
        assertEquals("Review threshold must be lower than decline threshold", exception.getMessage());
    }

    private void mockRules(int secondScore, RiskLevel secondRiskLevel) {
        Mockito.when(riskRuleEvaluator.evaluate(paymentRiskRequest))
                .thenReturn(List.of(
                        ruleResult("AMOUNT_RULE", 1, RiskLevel.LOW, "Low risk"),
                        ruleResult("IP_CHECK", secondScore, secondRiskLevel, "IP mismatch")
                ));
    }


    private PaymentRisk getSavedPayment() {
        ArgumentCaptor<PaymentRisk> captor = ArgumentCaptor.forClass(PaymentRisk.class);
        Mockito.verify(entityManager).persist(captor.capture());
        return captor.getValue();
    }

    private void verifyPaymentLookup(int times) {
        Mockito.verify(entityManager, times(times)).find(PaymentRisk.class, "PAY-001");
    }

    private void verifyRuleCalls() {
        Mockito.verify(riskRuleEvaluator, times(1)).evaluate(paymentRiskRequest);
        Mockito.verify(entityManager, times(1)).flush();
        Mockito.verifyNoMoreInteractions(entityManager, riskRuleEvaluator);
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
        return new RiskRuleResult(ruleName, score, riskLevel, reason);
    }

}
