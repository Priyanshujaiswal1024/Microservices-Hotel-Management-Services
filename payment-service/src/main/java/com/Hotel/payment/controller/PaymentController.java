package com.Hotel.payment.controller;

import com.Hotel.payment.dto.*;
import com.Hotel.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── INITIATE — USER only ──────────────────────────────────────
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDTO<PaymentInitiateResponseDTO>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Payment initiated",
                        paymentService.initiatePayment(request)));
    }

    // ── VERIFY — USER only ───────────────────────────────────────
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payment verified",
                paymentService.verifyPayment(request)));
    }

    // ── REFUND — ADMIN only ──────────────────────────────────────
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> refundPayment(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Refund initiated",
                paymentService.refundPayment(id)));
    }

    // ── WEBHOOK — Public (Razorpay calls this) ───────────────────
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    // ── GET BY ID ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payment fetched",
                paymentService.getPaymentById(id)));
    }

    // ── GET BY BOOKING ────────────────────────────────────────────
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getByBookingId(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payment fetched",
                paymentService.getPaymentByBookingId(bookingId)));
    }

    // ── GET BY USER ───────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<PaymentResponseDTO>>> getByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Payments fetched",
                paymentService.getPaymentsByUserId(userId)));
    }
}