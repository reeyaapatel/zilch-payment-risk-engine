package com.reeya.payment_risk_engine.rules;

import com.reeya.payment_risk_engine.model.risk.RiskLevel;
import com.reeya.payment_risk_engine.model.risk.RiskRuleResult;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.service.credit.CreditScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void constructor_whenThresholdsAreNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CreditScoreRule(creditScoreService, 0, 650, 50, 20)
        );

        // THEN
        assertEquals(
                "Invalid Credit score rule configuration: must have positive thresholds and values",
                exception.getMessage());
    }

    @Test
    public void constructor_whenValuesAreNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CreditScoreRule(creditScoreService, 500, 650, 0, 20)
        );

        // THEN
        assertEquals(
                "Invalid Credit score rule configuration: must have positive thresholds and values",
                exception.getMessage());
    }

    @Test
    public void constructor_whenHighRiskThresholdIsNotLowerThanMediumRiskThresholdThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CreditScoreRule(creditScoreService, 650, 650, 50, 20)
        );

        // THEN
        assertEquals(
                "Invalid Credit score rule configuration: High risk credit score threshold must be lower than medium risk credit score threshold",
                exception.getMessage());
    }

    @Test
    public void constructor_whenHighRiskValueIsNotGreaterThanMediumRiskValueThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CreditScoreRule(creditScoreService, 500, 650, 20, 20)
        );

        // THEN
        assertEquals(
                "Invalid Credit score rule configuration: High risk credit score value must be higher than medium risk credit score value",
                exception.getMessage());
    }

    private static Stream<Arguments> creditScoreCases() {
        return Stream.of(
                Arguments.of(OptionalInt.empty(), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        50,
                        RiskLevel.HIGH,
                        "Credit score not available"
                )),
                Arguments.of(OptionalInt.of(499), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        50,
                        RiskLevel.HIGH,
                        "Credit score is high risk"
                )),
                Arguments.of(OptionalInt.of(500), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        20,
                        RiskLevel.MEDIUM,
                        "Credit score is medium risk"
                )),
                Arguments.of(OptionalInt.of(649), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        20,
                        RiskLevel.MEDIUM,
                        "Credit score is medium risk"
                )),
                Arguments.of(OptionalInt.of(650), new RiskRuleResult(
                        "CREDIT_SCORE_CHECK",
                        0,
                        RiskLevel.LOW,
                        "Credit score is low risk"
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
