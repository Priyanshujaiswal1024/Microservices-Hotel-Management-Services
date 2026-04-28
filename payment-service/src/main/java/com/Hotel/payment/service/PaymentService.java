package com.Hotel.payment.service;

import com.Hotel.payment.dto.*;
import com.Hotel.payment.entity.*;
import com.Hotel.payment.entity.Payment;
import com.Hotel.payment.exception.ResourceNotFoundException;
import com.Hotel.payment.feignclient.BookingClient;
import com.Hotel.payment.repository.PaymentRepository;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final BookingClient bookingClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    // ── INITIATE PAYMENT ──────────────────────────────────────────
    @Transactional
    public PaymentInitiateResponseDTO initiatePayment(
            PaymentInitiateRequestDTO request) {
        try {
            // Amount paise mein (1 rupee = 100 paise)
            int amountInPaise = (int) (request.getAmount() * 100);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + request.getBookingId());
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            // Payment record save karo
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .razorpayOrderId(razorpayOrderId)
                    .status(PaymentStatus.PENDING)
                    .build();

            Payment saved = paymentRepository.save(payment);
            log.info("Payment initiated: orderId={} bookingId={}",
                    razorpayOrderId, request.getBookingId());

            return PaymentInitiateResponseDTO.builder()
                    .paymentId(saved.getId())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(request.getAmount())
                    .currency("INR")
                    .keyId(razorpayKeyId)
                    .bookingId(request.getBookingId())
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    // ── VERIFY PAYMENT ────────────────────────────────────────────
    @Transactional
    public PaymentResponseDTO verifyPayment(PaymentVerifyRequestDTO request) {

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + request.getPaymentId()));

        boolean isValid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (isValid) {
            // Payment SUCCESS
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // Booking CONFIRM karo
            bookingClient.updateBookingStatus(
                    payment.getBookingId(), "CONFIRMED");

            log.info("Payment verified ✅ paymentId={} bookingId={}",
                    request.getRazorpayPaymentId(), payment.getBookingId());

        } else {
            // Payment FAILED
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);

            // Booking FAIL karo
            bookingClient.updateBookingStatus(
                    payment.getBookingId(), "FAILED");

            log.warn("Payment verification FAILED ❌ bookingId={}",
                    payment.getBookingId());
        }

        return toResponseDTO(payment);
    }

    // ── REFUND ────────────────────────────────────────────────────
    @Transactional
    public PaymentResponseDTO refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException(
                    "Only successful payments can be refunded");
        }

        try {
            int amountInPaise = (int) (payment.getAmount() * 100);

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaise);

            Refund refund = razorpayClient.payments
                    .refund(payment.getRazorpayPaymentId(), refundRequest);

            String refundId = refund.get("id");
            String refundState = refund.get("status");

            payment.setRefundId(refundId);
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundStatus(
                    "processed".equals(refundState)
                            ? RefundStatus.PROCESSED
                            : RefundStatus.INITIATED
            );
            paymentRepository.save(payment);

            // Booking CANCEL karo
            bookingClient.updateBookingStatus(
                    payment.getBookingId(), "CANCELLED");

            log.info("Refund initiated ✅ refundId={} bookingId={}",
                    refundId, payment.getBookingId());

            return toResponseDTO(payment);

        } catch (RazorpayException e) {
            payment.setRefundStatus(RefundStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Refund FAILED ❌: {}", e.getMessage());
            throw new RuntimeException("Refund failed: " + e.getMessage());
        }
    }

    // ── WEBHOOK ───────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String payload, String signature) {

        // Webhook signature verify karo
        if (!verifyWebhookSignature(payload, signature)) {
            log.warn("Invalid webhook signature");
            throw new RuntimeException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");
        log.info("Webhook received: {}", eventType);

        switch (eventType) {
            case "payment.captured" -> {
                JSONObject paymentEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String orderId = paymentEntity.getString("order_id");
                String paymentId = paymentEntity.getString("id");

                paymentRepository.findByRazorpayOrderId(orderId)
                        .ifPresent(p -> {
                            if (p.getStatus() != PaymentStatus.SUCCESS) {
                                p.setRazorpayPaymentId(paymentId);
                                p.setStatus(PaymentStatus.SUCCESS);
                                paymentRepository.save(p);
                                bookingClient.updateBookingStatus(
                                        p.getBookingId(), "CONFIRMED");
                                log.info("Webhook: Booking {} confirmed",
                                        p.getBookingId());
                            }
                        });
            }
            case "payment.failed" -> {
                JSONObject paymentEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String orderId = paymentEntity.getString("order_id");

                paymentRepository.findByRazorpayOrderId(orderId)
                        .ifPresent(p -> {
                            p.setStatus(PaymentStatus.FAILED);
                            p.setFailureReason("Payment failed via webhook");
                            paymentRepository.save(p);
                            bookingClient.updateBookingStatus(
                                    p.getBookingId(), "FAILED");
                            log.warn("Webhook: Booking {} failed",
                                    p.getBookingId());
                        });
            }
            case "refund.processed" -> {
                JSONObject refundEntity = event
                        .getJSONObject("payload")
                        .getJSONObject("refund")
                        .getJSONObject("entity");

                String refundId = refundEntity.getString("id");
                paymentRepository.findAll().stream()
                        .filter(p -> refundId.equals(p.getRefundId()))
                        .findFirst()
                        .ifPresent(p -> {
                            p.setRefundStatus(RefundStatus.PROCESSED);
                            paymentRepository.save(p);
                            log.info("Refund processed: {}", refundId);
                        });
            }
        }
    }

    // ── GET PAYMENT ───────────────────────────────────────────────
    public PaymentResponseDTO getPaymentById(Long id) {
        return toResponseDTO(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + id)));
    }

    public PaymentResponseDTO getPaymentByBookingId(Long bookingId) {
        return toResponseDTO(paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for booking: " + bookingId)));
    }

    public List<PaymentResponseDTO> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────
    private boolean verifySignature(String orderId,
                                    String paymentId,
                                    String signature) {
        try {
            String data = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    razorpayKeySecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes());
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            String generated = HexFormat.of().formatHex(hash);
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private PaymentResponseDTO toResponseDTO(Payment p) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .bookingId(p.getBookingId())
                .userId(p.getUserId())
                .amount(p.getAmount())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .status(p.getStatus().name())
                .refundStatus(p.getRefundStatus() != null
                        ? p.getRefundStatus().name() : null)
                .refundId(p.getRefundId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}