//package com.reeya.payment_risk_engine.store;
//
//import com.reeya.payment_risk_engine.model.RiskRuleConfig;
//import com.reeya.payment_risk_engine.repository.RiskRuleConfigRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@Component
//public class RiskRuleConfigCache {
//
//    private final RiskRuleConfigRepository repository;
//    private final Map<String, List<RiskRuleConfig>> configsByRule = new ConcurrentHashMap<>();
//
//    public RiskRuleConfigCache(RiskRuleConfigRepository repository) {
//        this.repository = repository;
//    }
//
//    @PostConstruct
//    public void load()
//    {
//        configsByRule.putAll(repository.findAll()
//                .stream()
//                .filter(RiskRuleConfig::isActive)
//                .collect(Collectors.groupingBy(RiskRuleConfig::getRuleName)));
//    }
//
//    public List<RiskRuleConfig> getConfigs(String ruleName) {
//        return configsByRule.getOrDefault(ruleName, List.of());
//    }
//}
