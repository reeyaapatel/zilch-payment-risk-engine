
package com.reeya.payment_risk_engine.service;


import com.reeya.payment_risk_engine.model.*;
import com.reeya.payment_risk_engine.repository.PaymentRiskRepository;
import com.reeya.payment_risk_engine.rules.RiskRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
public class PaymentRiskServiceTest {


    @Mock
    private PaymentRiskRepository paymentRiskRepository;

    @Mock
    private RiskRule riskRule1;

    @Mock
    private RiskRule riskRule2;

    private PaymentRiskService paymentRiskService;

    private PaymentRiskRequest paymentRiskRequest;

    private final List<String> expectedReasons = List.of("Low risk", "IP mismatch");

    @BeforeEach
    public void setUp() {

        paymentRiskService = new PaymentRiskService(paymentRiskRepository, Arrays.asList(riskRule1, riskRule2));
        paymentRiskRequest = PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .amount(new BigDecimal(100))
                .merchantCountry("UK")
                .merchantName("MARKS&SPENCER")
                .currency("GBP")
                .buyerIp("1.2.3.4")
                .build();

        }

        @Test
        public void assessRisk_whenRiskScoreAboveHighRiskThreshold() {
            //GIVEN
            mockRules(1, 100, RiskLevel.HIGH);
            mockRepositorySave();

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            PaymentRisk savedPayment = verifySavedPayment();
            assertSavedPayment(savedPayment, 101, Status.DECLINED);
            assertResponse(response, 101, Status.DECLINED);
            verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsEqualToHighThreshold() {
        //GIVEN
        mockRules(1, 69, RiskLevel.HIGH);
        mockRepositorySave();

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 70, Status.DECLINED);
        assertResponse(response, 70, Status.DECLINED);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenRiskScoreIsWithinMediumThreshold() {
        //GIVEN
        mockRules(1, 42, RiskLevel.HIGH);
        mockRepositorySave();

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 43, Status.REQUIRES_REVIEW);
        assertResponse(response, 43, Status.REQUIRES_REVIEW);
        verifyRuleCalls();
    }
        @Test
        public void assessRisk_whenRiskScoreIsEqualToMediumThreshold() {
            //GIVEN
            mockRules(1, 39, RiskLevel.HIGH);
            mockRepositorySave();

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            PaymentRisk savedPayment = verifySavedPayment();
            assertSavedPayment(savedPayment, 40, Status.REQUIRES_REVIEW);
            assertResponse(response, 40, Status.REQUIRES_REVIEW);
            verifyRuleCalls();
    }


    @Test
    public void assessRisk_whenRiskScoreIsWithinLowThreshold() {
        //GIVEN
        mockRules(1, 10, RiskLevel.LOW);
        mockRepositorySave();

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        PaymentRisk savedPayment = verifySavedPayment();
        assertSavedPayment(savedPayment, 11, Status.APPROVED);
        assertResponse(response, 11, Status.APPROVED);
        verifyRuleCalls();
    }

    @Test
    public void assessRisk_whenPaymentRiskIsAlreadyCached() {
        //GIVEN
        mockRules(1, 100, RiskLevel.HIGH);
        mockRepositorySave();
        PaymentRiskResponse firstResponse = paymentRiskService.assessRisk(paymentRiskRequest);
        Mockito.clearInvocations(paymentRiskRepository, riskRule1, riskRule2);

        //WHEN
        PaymentRiskResponse cachedResponse = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        assertSame(firstResponse, cachedResponse);
        assertResponse(cachedResponse, 101, Status.DECLINED);
        Mockito.verifyNoInteractions(paymentRiskRepository, riskRule1, riskRule2);
    }

    @Test
    public void getPayment_whenPaymentRiskIsAlreadyCached() {
        //GIVEN
        mockRules(1, 100, RiskLevel.HIGH);
        mockRepositorySave();
        PaymentRiskResponse cachedAssessment = paymentRiskService.assessRisk(paymentRiskRequest);
        Mockito.clearInvocations(paymentRiskRepository, riskRule1, riskRule2);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.getPayment("PAY-001");

        //THEN
        assertSame(cachedAssessment, response);
        assertResponse(response, 101, Status.DECLINED);
        Mockito.verifyNoInteractions(paymentRiskRepository, riskRule1, riskRule2);
    }

    @Test
    public void getPayment_whenPaymentRiskIsFetchedFromDatabase() {
        //GIVEN
        PaymentRisk storedPayment = paymentRisk(40, Status.REQUIRES_REVIEW);
        Mockito.when(paymentRiskRepository.findById("PAY-001")).thenReturn(Optional.of(storedPayment));

        //WHEN
        PaymentRiskResponse response = paymentRiskService.getPayment("PAY-001");

        //THEN
        assertStoredPaymentResponse(response, 40, Status.REQUIRES_REVIEW);
        Mockito.verify(paymentRiskRepository).findById("PAY-001");
        Mockito.verifyNoInteractions(riskRule1, riskRule2);
        Mockito.verifyNoMoreInteractions(paymentRiskRepository);
    }

    private void mockRules(int firstScore, int secondScore, RiskLevel secondRiskLevel) {
        Mockito.when(riskRule1.evaluate(paymentRiskRequest))
                .thenReturn(ruleResult("HIGH_AMOUNT", firstScore, RiskLevel.LOW, "Low risk"));
        Mockito.when(riskRule2.evaluate(paymentRiskRequest))
                .thenReturn(ruleResult("IP_CHECK", secondScore, secondRiskLevel, "IP mismatch"));
    }

    private void mockRepositorySave() {
        Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private PaymentRisk verifySavedPayment() {
        ArgumentCaptor<PaymentRisk> captor = ArgumentCaptor.forClass(PaymentRisk.class);
        Mockito.verify(paymentRiskRepository).save(captor.capture());
        return captor.getValue();
    }

    private void verifyRuleCalls() {
        Mockito.verify(riskRule1).evaluate(paymentRiskRequest);
        Mockito.verify(riskRule2).evaluate(paymentRiskRequest);
        Mockito.verifyNoMoreInteractions(paymentRiskRepository, riskRule1, riskRule2);
    }

    private void assertSavedPayment(PaymentRisk savedPayment, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", savedPayment.getPaymentId());
        assertEquals(BigDecimal.valueOf(100), savedPayment.getAmount());
        assertEquals("GBP", savedPayment.getCurrency());
        assertEquals("MARKS&SPENCER", savedPayment.getMerchantName());
        assertEquals("UK", savedPayment.getMerchantCountry());
        assertEquals("1.2.3.4", savedPayment.getBuyerIp());
        assertEquals(expectedRiskScore, savedPayment.getRiskScore());
        assertEquals(expectedStatus, savedPayment.getStatus());
        assertEquals(expectedReasons, savedPayment.getReasons());
        assertNotNull(savedPayment.getCreatedAt());
    }

    private void assertResponse(PaymentRiskResponse response, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", response.getPaymentId());
        assertSame(paymentRiskRequest, response.getPaymentDetails());
        assertEquals(expectedRiskScore, response.getRiskScore());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertNotNull(response.getCreatedAt());
    }

    private void assertStoredPaymentResponse(PaymentRiskResponse response, int expectedRiskScore, Status expectedStatus) {
        assertEquals("PAY-001", response.getPaymentId());
        assertEquals(expectedRiskScore, response.getRiskScore());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedReasons, response.getReasons());
        assertEquals(Instant.parse("2026-05-29T10:15:30Z"), response.getCreatedAt());

        PaymentRiskRequest paymentDetails = response.getPaymentDetails();
        assertEquals("PAY-001", paymentDetails.getPaymentId());
        assertEquals(BigDecimal.valueOf(100), paymentDetails.getAmount());
        assertEquals("GBP", paymentDetails.getCurrency());
        assertEquals("MARKS&SPENCER", paymentDetails.getMerchantName());
        assertEquals("UK", paymentDetails.getMerchantCountry());
        assertEquals("1.2.3.4", paymentDetails.getBuyerIp());
    }

    private PaymentRisk paymentRisk(int riskScore, Status status) {
        return PaymentRisk.builder()
                .paymentId("PAY-001")
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("MARKS&SPENCER")
                .merchantCountry("UK")
                .buyerIp("1.2.3.4")
                .riskScore(riskScore)
                .status(status)
                .reasons(expectedReasons)
                .createdAt(Instant.parse("2026-05-29T10:15:30Z"))
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
