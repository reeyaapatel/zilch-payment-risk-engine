package com.reeya.payment_risk_engine.client;


import com.reeya.payment_risk_engine.model.api.IpGeoLocationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Slf4j
@Component
public class IpGeoLocationClient {


    private final RestClient ipGeoLocationClient;

    private static final String SUCCESS = "success";

    public IpGeoLocationClient(@Qualifier("ipGeoLocationRestClient") RestClient ipGeoLocationClient) {
        this.ipGeoLocationClient = ipGeoLocationClient;
    }

    public Optional<String> getCountry(String ipAddress) {

        try {
            IpGeoLocationResponse response = ipGeoLocationClient.get()
                    .uri("/{ipAddress}", ipAddress)
                    .retrieve()
                    .body(IpGeoLocationResponse.class);

            if (response == null
                    || !SUCCESS.equalsIgnoreCase(response.getSuccess())
                    || !StringUtils.hasText(response.getCountry())) {
                return Optional.empty();
            }

            return Optional.of(response.getCountry());

        } catch (RestClientException e)
        {
            return Optional.empty();
        }
    }


    }
