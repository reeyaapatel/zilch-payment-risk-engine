package com.reeya.payment_risk_engine.model.api;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response class for IP geolocation API
 */
@AllArgsConstructor
@Getter
@Setter
public class IpGeoLocationResponse
{

    private String status;
    private String countryCode;

}
