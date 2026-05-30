package com.reeya.payment_risk_engine.config;


import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class RiskRuleExecutorConfig {

    @Value("${risk.rule.executor.core.pool.size:4}")
    private int corePoolSize;

    @Value("${risk.rule.executor.max.pool.size:8}")
    private int maxPoolSize;

    @Value("${risk.rule.executor.queue.capacity:50}")
    private int queueCapacity;

    @Bean
    public Executor riskRuleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("risk-rule-");
        executor.initialize();
        return executor;
    }
}
