package com.reeya.payment_risk_engine.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reeya.payment_risk_engine.client.IpGeoLocationClient;
import com.reeya.payment_risk_engine.client.StubCreditScoreClient;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.model.persistence.PaymentRisk;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PaymentRiskIntegrationTest.DatabaseInitializer.class)
public class PaymentRiskIntegrationTest {


    private static final String DATABASE_URL = "jdbc:h2:mem:payment-risk-integration;DB_CLOSE_DELAY=-1"; // -> test specific
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private IpGeoLocationClient ipGeoLocationClient;

    @MockitoBean
    private StubCreditScoreClient creditScoreClient;

    @Test
    public void validate_IntegrationWithDatabaseAndAuthentication() throws Exception {
        LocalDate businessDate = LocalDate.parse("2026-05-30");
        Mockito.when(ipGeoLocationClient.getCountryCode("1.2.3.4"))
                .thenReturn(Optional.of("GB"));
        Mockito.when(creditScoreClient.calculateCreditScore("CUSTOMER-001", businessDate))
                .thenReturn(OptionalInt.of(700));

        Map<String, Object> request = Map.of(
                "paymentId", "IT-PAY-001",
                "customerId", "CUSTOMER-001",
                "businessDate", businessDate,
                "amount", BigDecimal.valueOf(100),
                "currency", "GBP",
                "merchantName", "ASOS",
                "merchantCountryCode", "GB",
                "buyerIp", "1.2.3.4"
        );

        mockMvc.perform(post("/payments/risk")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("IT-PAY-001"))
                .andExpect(jsonPath("$.customerId").value("CUSTOMER-001"))
                .andExpect(jsonPath("$.businessDate").value("2026-05-30"))
                .andExpect(jsonPath("$.merchantCountryCode").value("GB"))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reasons", hasSize(3)));


        // Validate payment directly in db
        PaymentRisk persistedPaymentRisk = entityManager.find(PaymentRisk.class, "IT-PAY-001");
        assertNotNull(persistedPaymentRisk);
        assertEquals("IT-PAY-001", persistedPaymentRisk.getPaymentId());
        assertEquals("CUSTOMER-001", persistedPaymentRisk.getCustomerId());
        assertEquals(businessDate, persistedPaymentRisk.getBusinessDate());
        assertEquals(BigDecimal.valueOf(100), persistedPaymentRisk.getAmount());
        assertEquals("GBP", persistedPaymentRisk.getCurrency());
        assertEquals("ASOS", persistedPaymentRisk.getMerchantName());
        assertEquals("GB", persistedPaymentRisk.getMerchantCountryCode());
        assertEquals("1.2.3.4", persistedPaymentRisk.getBuyerIp());
        assertEquals(0, persistedPaymentRisk.getRiskScore());
        assertEquals(Status.APPROVED, persistedPaymentRisk.getStatus());
        assertEquals(3, persistedPaymentRisk.getReasons().size());
        assertNotNull(persistedPaymentRisk.getCreatedAt());
        assertNotNull(persistedPaymentRisk.getLastUpdatedAt());

    }

    @Test
    public void validate_WhenNotAuthenticationWithUsernameAndPassword() throws Exception {
        mockMvc.perform(get("/payments/IT-PAY-001"))
                .andExpect(status().isUnauthorized());
    }

    public static class DatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Flyway.configure()
                    .dataSource(DATABASE_URL, "sa", "")
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            TestPropertyValues.of(
                    "spring.datasource.url=" + DATABASE_URL,
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.jpa.hibernate.ddl-auto=validate",
                    "spring.flyway.enabled=false"
            ).applyTo(applicationContext);
        }
    }
}
