//package com.reeya.payment_risk_engine.service;
//
//import com.reeya.payment_risk_engine.model.*;
//import com.reeya.payment_risk_engine.repository.PaymentRiskRepository;
//import com.reeya.payment_risk_engine.rules.RiskRule;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentRiskServiceTest {
//
//    @Mock
//    private PaymentRiskRepository repository;
//
//    @Mock
//    private RiskRule rule1;
//
//    @Mock
//    private RiskRule rule2;
//
//    @InjectMocks
//    private PaymentRiskService paymentRiskService;
//
//    @Test
//    void shouldApprovePaymentWhenRiskScoreIsBelow40() {
//        PaymentRiskRequest request = PaymentRiskRequest.builder()
//                .paymentId("PAY123")
//                .build();
//
//        when(rule1.evaluate(request)).thenReturn(new RiskRuleResult("HIGH_AMOUNT", 50, RiskLevel.HIGH, "Threshold higher than amount"));
//        when(rule2.evaluate(request)).thenReturn(new RiskRuleResult("COUNTRY", 100, RiskLevel.HIGH, "Payment to different country"));
//
//        PaymentRiskService service =
//                new PaymentRiskService(repository, List.of(rule1, rule2));
//
//        PaymentRiskResponse response = service.assessRisk(request);
//
//        assertEquals("PAY123", response.getPaymentId());
//        assertEquals(Status.APPROVED, response.getStatus());
//
//        verify(repository).save(argThat(paymentRisk ->
//                paymentRisk.getPaymentId().equals("PAY123")
//                        && paymentRisk.getStatus() == Status.APPROVED
//        ));
//    }
//
//    @Test
//    void shouldRequireReviewWhenRiskScoreIsBetween40And69() {
//        PaymentRiskRequest request = PaymentRiskRequest.builder()
//                .paymentId("PAY456")
//                .build();
//
//        when(rule1.evaluate(request)).thenReturn(new RiskRuleResult("RULE_1", 25));
//        when(rule2.evaluate(request)).thenReturn(new RiskRuleResult("RULE_2", 20));
//
//        PaymentRiskService service =
//                new PaymentRiskService(repository, List.of(rule1, rule2));
//
//        PaymentRiskResponse response = service.assessRisk(request);
//
//        assertEquals(Status.REQUIRES_REVIEW, response.getStatus());
//
//        verify(repository).save(argThat(paymentRisk ->
//                paymentRisk.getStatus() == Status.REQUIRES_REVIEW
//        ));
//    }
//
//    @Test
//    void shouldDeclinePaymentWhenRiskScoreIs70OrMore() {
//        PaymentRiskRequest request = PaymentRiskRequest.builder()
//                .paymentId("PAY789")
//                .build();
//
//        when(rule1.evaluate(request)).thenReturn(new RiskRuleResult("RULE_1", 40));
//        when(rule2.evaluate(request)).thenReturn(new RiskRuleResult("RULE_2", 30));
//
//        PaymentRiskService service =
//                new PaymentRiskService(repository, List.of(rule1, rule2));
//
//        PaymentRiskResponse response = service.assessRisk(request);
//
//        assertEquals(Status.DECLINED, response.getStatus());
//
//        verify(repository).save(argThat(paymentRisk ->
//                paymentRisk.getStatus() == Status.DECLINED
//        ));
//    }
//}
