package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.RiskLevel;
import com.reeya.payment_risk_engine.model.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.service.CreditScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CreditScoreRuleTest {

    @Mock
    private CreditScoreService creditScoreService;

    private CreditScoreRule creditScoreRule;

    @BeforeEach
    public void setUp() {
        creditScoreRule = new CreditScoreRule(creditScoreService, 500, 650, 50, 20);
    }

    @ParameterizedTest
    @MethodSource("creditScoreCases")
    public void evaluate_shouldReturnExpectedRisk(
            int creditScore,
            RiskRuleResult expectedResult
    ) {
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(creditScoreService.getCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30")))
                .thenReturn(OptionalInt.of(creditScore));

        RiskRuleResult result = creditScoreRule.evaluate(request);

        assertEquals(expectedResult, result);
        Mockito.verify(creditScoreService).getCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30"));
        Mockito.verifyNoMoreInteractions(creditScoreService);
    }

    private static Stream<Arguments> creditScoreCases() {
        return Stream.of(
                Arguments.of(499, new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        50,
                        RiskLevel.HIGH,
                        "Credit score is high risk"
                )),
                Arguments.of(500, new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        20,
                        RiskLevel.MEDIUM,
                        "Credit score is medium risk"
                )),
                Arguments.of(649, new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        20,
                        RiskLevel.MEDIUM,
                        "Credit score is medium risk"
                )),
                Arguments.of(650, new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        0,
                        RiskLevel.LOW,
                        "Credit score is low risk"
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("missingCreditScoreCases")
    public void evaluate_whenCreditScoreIsMissingShouldReturnHighRisk(
            OptionalInt creditScore,
            RiskRuleResult expectedResult
    ) {
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(creditScoreService.getCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30")))
                .thenReturn(creditScore);

        RiskRuleResult result = creditScoreRule.evaluate(request);

        assertEquals(expectedResult, result);
        Mockito.verify(creditScoreService).getCreditScore("CUSTOMER-001", LocalDate.parse("2026-05-30"));
        Mockito.verifyNoMoreInteractions(creditScoreService);
    }

    private static Stream<Arguments> missingCreditScoreCases() {
        return Stream.of(
                Arguments.of(OptionalInt.empty(), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        50,
                        RiskLevel.HIGH,
                        "Credit score not available"
                ))
        );
    }

    private PaymentRiskRequest paymentRiskRequest() {
        return PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("ASOS")
                .merchantCountryCode("GB")
                .buyerIp("1.2.3.4")
                .build();
    }
}
