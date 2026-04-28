package com.Hotel.payment.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String status;
    private String refundStatus;
    private String refundId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}