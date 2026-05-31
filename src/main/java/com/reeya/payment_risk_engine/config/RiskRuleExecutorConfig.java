package com.reeya.payment_risk_engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Config for the risk rule executor to add parallelism to the risk rule execution.
 */
@Configuration
public class RiskRuleExecutorConfig {

    @Value("${risk.rule.executor.core.pool.size}")
    private int corePoolSize;

    @Value("${risk.rule.executor.max.pool.size}")
    private int maxPoolSize;

    @Value("${risk.rule.executor.queue.capacity}")
    private int queueCapacity;

    private static final String THREAD_PREFIX = "risk-rule-";

    @Bean
    public Executor riskRuleExecutor() {
        if (corePoolSize <= 0 || maxPoolSize <= 0 || queueCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Executor configuration: must have positive core pool size, max pool size and queue capacity");
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(THREAD_PREFIX);
        executor.initialize();
        return executor;
    }
}
