package com.reeya.payment_risk_engine.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reeya.payment_risk_engine.model.api.IpGeoLocationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class IpGeoLocationClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private IpGeoLocationClient client;

    @BeforeEach
    public void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://ip-api.com/json");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new IpGeoLocationClient(builder.build());
    }

    @Test
    public void getCountry_whenApiReturnsSuccessReturnsCountry() throws JsonProcessingException {
        server.expect(requestTo("https://ip-api.com/json/1.2.3.4"))
                .andRespond(withSuccess(apiResponse("success", "GB"), MediaType.APPLICATION_JSON));

        Optional<String> country = client.getCountryCode("1.2.3.4");

        assertEquals(Optional.of("GB"), country);
        server.verify();
    }

    @Test
    public void getCountry_whenApiResponseIsNotSuccessfulReturnsEmpty() throws JsonProcessingException {
        server.expect(requestTo("https://ip-api.com/json/1.2.3.4"))
                .andRespond(withSuccess(apiResponse("fail", "GB"), MediaType.APPLICATION_JSON));

        Optional<String> country = client.getCountryCode("1.2.3.4");

        assertTrue(country.isEmpty());
        server.verify();
    }

    @Test
    public void getCountry_whenCountryIsBlankReturnsEmpty() throws JsonProcessingException {
        server.expect(requestTo("https://ip-api.com/json/1.2.3.4"))
                .andRespond(withSuccess(apiResponse("success", " "), MediaType.APPLICATION_JSON));

        Optional<String> country = client.getCountryCode("1.2.3.4");

        assertTrue(country.isEmpty());
        server.verify();
    }

    @Test
    public void getCountry_whenClientThrowsReturnsEmpty() {
        server.expect(requestTo("https://ip-api.com/json/1.2.3.4"))
                .andRespond(withServerError());

        Optional<String> country = client.getCountryCode("1.2.3.4");

        assertTrue(country.isEmpty());
        server.verify();
    }

    private String apiResponse(String success, String country) throws JsonProcessingException {
        return objectMapper.writeValueAsString(new IpGeoLocationResponse(success, country));
    }
}
