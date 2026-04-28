package com.Hotel.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentVerifyRequestDTO {

    // Hamara DB payment ID
    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    // Razorpay se aaya order ID
    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;

    // Razorpay se aaya payment ID (after payment done)
    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;

    // Razorpay se aaya signature (verify karne ke liye)
    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;
}