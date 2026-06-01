package com.reeya.payment_risk_engine.model.api;

import com.reeya.payment_risk_engine.model.risk.Status;
import jakarta.validation.constraints.NotNull;

/**
 * PaymentStatusUpdate for incoming patch status requests.
 */
public record PaymentStatusUpdate(
        @NotNull Status status
) {
}
