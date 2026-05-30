package com.reeya.payment_risk_engine.model.api;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentRiskRequest
{
    @NotBlank
    private String paymentId;

    @NotNull
    private LocalDate businessDate;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String merchantName;

    @NotBlank
    @Size(min = 2, max = 2)
    @Pattern(regexp = "^[A-Z]{2,3}$", message = "ISO country code required")
    @NotBlank
    private String merchantCountryCode;

    @NotBlank
    @Pattern(
            regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$",
            message = "buyerIp must be a valid IPv4 address"
    )
    private String buyerIp;
}
