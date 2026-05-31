package com.reeya.payment_risk_engine.client;

import java.util.Optional;

/**
 * Provider contract for resolving an IP address to a country code.
 */
public interface IpGeoLocationProvider {

    Optional<String> getCountryCode(String ipAddress);
}
