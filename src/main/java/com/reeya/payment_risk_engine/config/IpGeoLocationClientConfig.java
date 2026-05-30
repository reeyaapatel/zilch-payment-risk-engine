package com.reeya.payment_risk_engine.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class IpGeoLocationClientConfig {



    @Bean
    public RestClient ipGeoLocationRestClient()
    {
        return RestClient.builder().baseUrl("http://ip-api.com/json/").build();
    }
}
