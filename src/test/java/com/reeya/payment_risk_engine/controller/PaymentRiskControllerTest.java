package com.reeya.payment_risk_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reeya.payment_risk_engine.config.SecurityConfig;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.service.PaymentRiskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = PaymentRiskController.class,
        properties = {
                "spring.security.user.name=admin",
                "spring.security.user.password=password"
        }
)
@Import(SecurityConfig.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
public class PaymentRiskControllerTest {

    private static final String USERNAME = "admin";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // required for BD serialization

    @MockitoBean
    private PaymentRiskService paymentRiskService;

    @Test
    public void assessRisk_shouldReturnCreatedPaymentRiskResponse() throws Exception {
        // GIVEN
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(paymentRiskService.assessRisk(Mockito.any(PaymentRiskRequest.class)))
                .thenReturn(paymentRiskResponse(request));

        // WHEN + THEN
        mockMvc.perform(post("/payments/risk")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.customerId").value("CUSTOMER-001"))
                .andExpect(jsonPath("$.riskScore").value(101))
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reasons[0]").value("Low risk"))
                .andExpect(jsonPath("$.reasons[1]").value("IP mismatch"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.businessDate").value("2026-05-30"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.merchantName").value("MARKS&SPENCER"))
                .andExpect(jsonPath("$.merchantCountryCode").value("UK"))
                .andExpect(jsonPath("$.buyerIp").value("1.2.3.4"));

        Mockito.verify(paymentRiskService).assessRisk(Mockito.any(PaymentRiskRequest.class));
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    public void assessRisk_whenRequestIsInvalidShouldReturnBadRequest() throws Exception {
        // GIVEN
        PaymentRiskRequest request = PaymentRiskRequest.builder()
                .paymentId("")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.ZERO)
                .currency("") //-- IS BLANK
                .merchantName("MARKS&SPENCER")
                .merchantCountryCode("UK")
                .buyerIp("1.2.3.4")
                .build();

        // WHEN + THEN
        mockMvc.perform(post("/payments/risk")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));

        Mockito.verifyNoInteractions(paymentRiskService);
    }

    @Test
    public void getPayment_shouldReturnPaymentRiskResponse() throws Exception {
        // GIVEN
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(paymentRiskService.getPaymentRiskResponse("PAY-001")).thenReturn(paymentRiskResponse(request));

        // WHEN + THEN
        mockMvc.perform(get("/payments/PAY-001")
                        .with(user(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.riskScore").value(101))
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reasons[0]").value("Low risk"))
                .andExpect(jsonPath("$.reasons[1]").value("IP mismatch"))
                .andExpect(jsonPath("$.amount").value(100));

        Mockito.verify(paymentRiskService).getPaymentRiskResponse("PAY-001");
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    public void updateStatus_shouldReturnUpdatedPaymentRiskResponse() throws Exception {
        // GIVEN
        PaymentRiskRequest request = paymentRiskRequest();
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);
        Mockito.when(paymentRiskService.updateStatus(Mockito.eq("PAY-001"), Mockito.any(PaymentStatusUpdate.class)))
                .thenReturn(paymentRiskResponse(request, Status.APPROVED));

        // WHEN + THEN
        mockMvc.perform(patch("/payments/PAY-001/status")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.amount").value(100));

        Mockito.verify(paymentRiskService).updateStatus(Mockito.eq("PAY-001"), Mockito.any(PaymentStatusUpdate.class));
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    public void getPayment_whenAuthenticationIsMissingShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/payments/PAY-001"))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(paymentRiskService);
    }

    @Test
    public void getPayment_whenPaymentDoesNotExistShouldReturnNotFound() throws Exception {
        // GIVEN
        Mockito.when(paymentRiskService.getPaymentRiskResponse("missing-id"))
                .thenThrow(new IllegalArgumentException("Payment not found: missing-id"));

        // WHEN + THEN
        mockMvc.perform(get("/payments/missing-id")
                        .with(user(USERNAME)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payment not found: missing-id"));

        Mockito.verify(paymentRiskService).getPaymentRiskResponse("missing-id");
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    public void updateStatus_whenPaymentDoesNotRequireReviewShouldReturnConflict() throws Exception {
        // GIVEN
        PaymentStatusUpdate update = new PaymentStatusUpdate(Status.APPROVED);
        Mockito.when(paymentRiskService.updateStatus(Mockito.eq("PAY-001"), Mockito.any(PaymentStatusUpdate.class)))
                .thenThrow(new IllegalStateException("Payment status can only be updated when it requires review"));

        // WHEN + THEN
        mockMvc.perform(patch("/payments/PAY-001/status")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Payment status can only be updated when it requires review"));

        Mockito.verify(paymentRiskService).updateStatus(Mockito.eq("PAY-001"), Mockito.any(PaymentStatusUpdate.class));
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    public void updateStatus_whenRequestIsInvalidShouldReturnBadRequest() throws Exception {
        // GIVEN
        PaymentStatusUpdate update = new PaymentStatusUpdate(null);

        // WHEN + THEN
        mockMvc.perform(patch("/payments/PAY-001/status")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));

        Mockito.verifyNoInteractions(paymentRiskService);
    }

    @Test
    public void updateStatus_whenStatusIsInvalidEnumShouldReturnBadRequest() throws Exception {
        Map<String, String> request = Map.of("status", "notvalidstatusforenum");

        mockMvc.perform(patch("/payments/PAY-001/status")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));

        Mockito.verifyNoInteractions(paymentRiskService);
    }

    private PaymentRiskRequest paymentRiskRequest() {
        return PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .customerId("CUSTOMER-001")
                .businessDate(LocalDate.parse("2026-05-30"))
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("MARKS&SPENCER")
                .merchantCountryCode("UK")
                .buyerIp("1.2.3.4")
                .build();
    }

    private PaymentRiskResponse paymentRiskResponse(PaymentRiskRequest request) {
        return paymentRiskResponse(request, Status.DECLINED);
    }

    private PaymentRiskResponse paymentRiskResponse(PaymentRiskRequest request, Status status) {
        return PaymentRiskResponse.builder()
                .paymentId("PAY-001")
                .customerId(request.getCustomerId())
                .businessDate(request.getBusinessDate())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantName(request.getMerchantName())
                .merchantCountryCode(request.getMerchantCountryCode())
                .buyerIp(request.getBuyerIp())
                .riskScore(101)
                .status(status)
                .reasons(List.of("Low risk", "IP mismatch"))
                .createdAt(Instant.parse("2026-05-29T10:15:30Z"))
                .build();
    }
}
