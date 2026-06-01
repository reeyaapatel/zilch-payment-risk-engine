package com.reeya.payment_risk_engine.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RiskRuleExecutorConfigTest {

    @Test
    public void riskRuleExecutor_whenConfigurationIsValidCreatesExecutor() {
        // GIVEN
        RiskRuleExecutorConfig config = new RiskRuleExecutorConfig(2, 4, 10);

        // WHEN
        Executor executor = config.riskRuleExecutor();

        // THEN
        ThreadPoolTaskExecutor taskExecutor = assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        assertEquals(2, taskExecutor.getCorePoolSize());
        assertEquals(4, taskExecutor.getMaxPoolSize());
        assertEquals("risk-rule-", taskExecutor.getThreadNamePrefix());

        taskExecutor.shutdown();
    }

    @Test
    public void riskRuleExecutor_whenCorePoolSizeIsNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RiskRuleExecutorConfig(0, 4, 10)
        );

        // THEN
        assertEquals(expectedErrorMessage(), exception.getMessage());
    }

    @Test
    public void riskRuleExecutor_whenMaxPoolSizeIsNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RiskRuleExecutorConfig(2, 0, 10)
        );

        // THEN
        assertEquals(expectedErrorMessage(), exception.getMessage());
    }

    @Test
    public void riskRuleExecutor_whenQueueCapacityIsNotPositiveThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RiskRuleExecutorConfig(2, 4, 0)
        );

        // THEN
        assertEquals(expectedErrorMessage(), exception.getMessage());
    }

    private String expectedErrorMessage() {
        return "Invalid Executor configuration: must have positive core pool size, max pool size and queue capacity";
    }
}
