package com.reeya.payment_risk_engine.model.api;

/**
 * Response class for IP geolocation API.
 */
public record IpGeoLocationResponse(
        String status,
        String countryCode
) {
}
