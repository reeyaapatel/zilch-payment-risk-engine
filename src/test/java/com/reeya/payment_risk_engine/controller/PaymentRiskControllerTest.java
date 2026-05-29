package com.reeya.payment_risk_engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reeya.payment_risk_engine.model.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.Status;
import com.reeya.payment_risk_engine.service.PaymentRiskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentRiskController.class)
class PaymentRiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @MockitoBean
    private PaymentRiskService paymentRiskService;

    @Test
    void assessRisk_shouldReturnCreatedPaymentRiskResponse() throws Exception {
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(paymentRiskService.assessRisk(Mockito.any(PaymentRiskRequest.class)))
                .thenReturn(paymentRiskResponse(request));

        mockMvc.perform(post("/payments/risk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.riskScore").value(101))
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reasons[0]").value("Low risk"))
                .andExpect(jsonPath("$.reasons[1]").value("IP mismatch"))
                .andExpect(jsonPath("$.paymentDetails.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.paymentDetails.amount").value(100))
                .andExpect(jsonPath("$.paymentDetails.currency").value("GBP"))
                .andExpect(jsonPath("$.paymentDetails.merchantName").value("MARKS&SPENCER"))
                .andExpect(jsonPath("$.paymentDetails.merchantCountry").value("UK"))
                .andExpect(jsonPath("$.paymentDetails.buyerIp").value("1.2.3.4"));

        Mockito.verify(paymentRiskService).assessRisk(Mockito.any(PaymentRiskRequest.class));
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    @Test
    void getPayment_shouldReturnPaymentRiskResponse() throws Exception {
        PaymentRiskRequest request = paymentRiskRequest();
        Mockito.when(paymentRiskService.getPayment("PAY-001")).thenReturn(paymentRiskResponse(request));

        mockMvc.perform(get("/payments/PAY-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY-001"))
                .andExpect(jsonPath("$.riskScore").value(101))
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reasons[0]").value("Low risk"))
                .andExpect(jsonPath("$.reasons[1]").value("IP mismatch"))
                .andExpect(jsonPath("$.paymentDetails.paymentId").value("PAY-001"));

        Mockito.verify(paymentRiskService).getPayment("PAY-001");
        Mockito.verifyNoMoreInteractions(paymentRiskService);
    }

    private PaymentRiskRequest paymentRiskRequest() {
        return PaymentRiskRequest.builder()
                .paymentId("PAY-001")
                .amount(BigDecimal.valueOf(100))
                .currency("GBP")
                .merchantName("MARKS&SPENCER")
                .merchantCountry("UK")
                .buyerIp("1.2.3.4")
                .build();
    }

    private PaymentRiskResponse paymentRiskResponse(PaymentRiskRequest request) {
        return PaymentRiskResponse.builder()
                .paymentId("PAY-001")
                .paymentDetails(request)
                .riskScore(101)
                .status(Status.DECLINED)
                .reasons(List.of("Low risk", "IP mismatch"))
                .createdAt(Instant.parse("2026-05-29T10:15:30Z"))
                .build();
    }
}
