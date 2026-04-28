package com.Hotel.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PaymentInitiateRequestDTO {

    @NotNull(message = "Booking ID required")
    private Long bookingId;

    @NotNull(message = "User ID required")
    private Long userId;

    @NotNull(message = "Amount required")
    private Double amount;
}