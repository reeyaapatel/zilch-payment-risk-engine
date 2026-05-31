package com.reeya.payment_risk_engine.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Config for the IP Geo Location API client.
 */
@Configuration
public class IpGeoLocationClientConfig {

    private final String ipEndpoint;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public IpGeoLocationClientConfig(
            @Value("${ip.geo.location.api.endpoint}") @NotBlank String ipEndpoint,
            @Value("${ip.geo.location.api.connect.timeout}") @NotNull Duration connectTimeout,
            @Value("${ip.geo.location.api.read.timeout}") @NotNull Duration readTimeout
    ) {
        this.ipEndpoint = ipEndpoint;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Bean
    public RestClient ipGeoLocationRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(ipEndpoint)
                .requestFactory(requestFactory)
                .build();
    }
}
