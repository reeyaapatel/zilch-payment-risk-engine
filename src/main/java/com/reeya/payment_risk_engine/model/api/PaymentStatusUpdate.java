package com.reeya.payment_risk_engine.model.api;

import com.reeya.payment_risk_engine.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdate {

    @NotNull
    private Status status;
}
