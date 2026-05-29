
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private PaymentRisk paymentRisk;
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
        paymentRisk = PaymentRisk.builder()
                .paymentId("PAY-001")
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("MARKS&SPENCER")
                .merchantCountry("GB")
                .buyerIp("1.2.3.4")
                .riskScore(101)
                .status(Status.DECLINED)
                .reasons(List.of(
                        "Low risk",
                        "IP mismatch"
                ))
                .createdAt(Instant.parse("2025-05-29T00:00:00Z"))
                .build();

        }

        @Test
        public void assessRisk_whenRiskScoreAboveHighRiskThreshold() {
            //GIVEN
            Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("HIGH_AMOUNT").score(1).riskLevel(RiskLevel.LOW).reason("Low risk").build());
            Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("IP_CHECK").score(100).riskLevel(RiskLevel.HIGH).reason("IP mismatch").build());
            Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            ArgumentCaptor<PaymentRisk> captor =
                    ArgumentCaptor.forClass(PaymentRisk.class);

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            Mockito.verify(paymentRiskRepository).save(captor.capture());
            PaymentRisk savedPayment = captor.getValue();
            assertEquals(101, savedPayment.getRiskScore());
            assertEquals(Status.DECLINED, savedPayment.getStatus());
            assertEquals(List.of("Low risk", "IP mismatch"), savedPayment.getReasons()
            );
    }

    @Test
    public void assessRisk_whenRiskScoreIsEqualToHighThreshold() {
        //GIVEN
        Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("HIGH_AMOUNT").score(1).riskLevel(RiskLevel.LOW).reason("Low risk").build());
        Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("IP_CHECK").score(69).riskLevel(RiskLevel.HIGH).reason("IP mismatch").build());
        Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<PaymentRisk> captor =
                ArgumentCaptor.forClass(PaymentRisk.class);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        Mockito.verify(paymentRiskRepository).save(captor.capture());
        PaymentRisk savedPayment = captor.getValue();
        assertEquals(70, savedPayment.getRiskScore());
        assertEquals(Status.DECLINED, savedPayment.getStatus());
        assertEquals(List.of("Low risk", "IP mismatch"), savedPayment.getReasons()
        );
    }

    @Test
    public void assessRisk_whenRiskScoreIsWithinMediumThreshold() {
        //GIVEN
        Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("HIGH_AMOUNT").score(1).riskLevel(RiskLevel.LOW).reason("Low risk").build());
        Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("IP_CHECK").score(42).riskLevel(RiskLevel.HIGH).reason("IP mismatch").build());
        Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<PaymentRisk> captor =
                ArgumentCaptor.forClass(PaymentRisk.class);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        Mockito.verify(paymentRiskRepository).save(captor.capture());
        PaymentRisk savedPayment = captor.getValue();
        assertEquals(43, savedPayment.getRiskScore());
        assertEquals(Status.REQUIRES_REVIEW, savedPayment.getStatus());
        assertEquals(List.of("Low risk", "IP mismatch"), savedPayment.getReasons()
        );
    }
        @Test
        public void assessRisk_whenRiskScoreIsEqualToMediumThreshold() {
            //GIVEN
            Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("HIGH_AMOUNT").score(1).riskLevel(RiskLevel.LOW).reason("Low risk").build());
            Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("IP_CHECK").score(39).riskLevel(RiskLevel.HIGH).reason("IP mismatch").build());
            Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            ArgumentCaptor<PaymentRisk> captor =
                    ArgumentCaptor.forClass(PaymentRisk.class);

            //WHEN
            PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

            //THEN
            Mockito.verify(paymentRiskRepository).save(captor.capture());
            PaymentRisk savedPayment = captor.getValue();
            assertEquals(40, savedPayment.getRiskScore());
            assertEquals(Status.REQUIRES_REVIEW, savedPayment.getStatus());
            assertEquals(List.of("Low risk", "IP mismatch"), savedPayment.getReasons()
            );
    }


    @Test
    public void assessRisk_whenRiskScoreIsWithinLowThreshold() {
        //GIVEN
        Mockito.when(riskRule1.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("HIGH_AMOUNT").score(1).riskLevel(RiskLevel.LOW).reason("Low risk").build());
        Mockito.when(riskRule2.evaluate(paymentRiskRequest)).thenReturn(RiskRuleResult.builder().ruleName("IP_CHECK").score(10).riskLevel(RiskLevel.LOW).reason("IP mismatch").build());
        Mockito.when(paymentRiskRepository.save(Mockito.any(PaymentRisk.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<PaymentRisk> captor =
                ArgumentCaptor.forClass(PaymentRisk.class);

        //WHEN
        PaymentRiskResponse response = paymentRiskService.assessRisk(paymentRiskRequest);

        //THEN
        Mockito.verify(paymentRiskRepository).save(captor.capture());
        PaymentRisk savedPayment = captor.getValue();
        assertEquals(11, savedPayment.getRiskScore());
        assertEquals(Status.APPROVED, savedPayment.getStatus());
        assertEquals(List.of("Low risk", "IP mismatch"), savedPayment.getReasons()
        );
    }


}
