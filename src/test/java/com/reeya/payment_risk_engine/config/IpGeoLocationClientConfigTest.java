package com.reeya.payment_risk_engine.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IpGeoLocationClientConfigTest {

    @Test
    public void constructor_whenEndpointIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig(null, Duration.ofSeconds(1), Duration.ofSeconds(2))
        );

        // THEN
        assertEquals("IP Geo Location API endpoint must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenEndpointIsBlankThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig(" ", Duration.ofSeconds(1), Duration.ofSeconds(2))
        );

        // THEN
        assertEquals("IP Geo Location API endpoint must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenConnectTimeoutIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig("http://ip-api.com/json/", null, Duration.ofSeconds(2))
        );

        // THEN
        assertEquals("Timeouts must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenReadTimeoutIsNullThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig("http://ip-api.com/json/", Duration.ofSeconds(1), null)
        );

        // THEN
        assertEquals("Timeouts must be provided", exception.getMessage());
    }

    @Test
    public void constructor_whenConnectTimeoutIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig(
                        "http://ip-api.com/json/",
                        Duration.ofMillis(-1),
                        Duration.ofSeconds(2))
        );

        // THEN
        assertEquals("Timeouts must be non-negative", exception.getMessage());
    }

    @Test
    public void constructor_whenReadTimeoutIsNegativeThrowsError() {
        // WHEN
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IpGeoLocationClientConfig(
                        "http://ip-api.com/json/",
                        Duration.ofSeconds(1),
                        Duration.ofMillis(-1))
        );

        // THEN
        assertEquals("Timeouts must be non-negative", exception.getMessage());
    }
}
