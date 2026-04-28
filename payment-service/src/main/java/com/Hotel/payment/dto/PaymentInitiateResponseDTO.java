package com.Hotel.payment.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentInitiateResponseDTO {
    private Long paymentId;
    private String razorpayOrderId;
    private Double amount;
    private String currency;
    private String keyId;       // frontend ko chahiye
    private Long bookingId;
}