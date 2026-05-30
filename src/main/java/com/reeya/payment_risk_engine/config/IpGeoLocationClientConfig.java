package com.reeya.payment_risk_engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Config for the IP Geo Location API client.
 */
@Configuration
public class IpGeoLocationClientConfig {

    private final String ipEndpoint;

    public IpGeoLocationClientConfig(@Value("${ip.geo.location.api.endpoint:http://ip-api.com/json/}") String ipEndpoint)
    {
        this.ipEndpoint = ipEndpoint;
    }

    @Bean
    public RestClient ipGeoLocationRestClient()
    {
        return RestClient.builder().baseUrl(ipEndpoint).build();
    }
}
