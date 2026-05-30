package com.reeya.payment_risk_engine.controller;
import com.reeya.payment_risk_engine.model.api.PaymentRiskRequest;
import com.reeya.payment_risk_engine.model.api.PaymentRiskResponse;
import com.reeya.payment_risk_engine.model.api.PaymentStatusUpdate;
import com.reeya.payment_risk_engine.service.PaymentRiskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Enpoints for the Payment Risk Engine
 */
@RestController
@RequestMapping("/payments")
public class PaymentRiskController {

    private final PaymentRiskService paymentRiskService;

    public PaymentRiskController(PaymentRiskService paymentRiskService) {
        this.paymentRiskService = paymentRiskService;
    }

    @PostMapping("/risk")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentRiskResponse assessRisk(@Valid @RequestBody PaymentRiskRequest request)
    {
        return paymentRiskService.assessRisk(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentRiskResponse getPayment(@PathVariable String paymentId)
    {
        return paymentRiskService.getPaymentRiskResponse(paymentId);
    }

    @PatchMapping("/{paymentId}/status")
    public PaymentRiskResponse updateStatus(@PathVariable String paymentId, @Valid @RequestBody PaymentStatusUpdate update)
    {
        return paymentRiskService.updateStatus(paymentId, update);
    }
}
