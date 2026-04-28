package com.Hotel.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpEventProducer {

    private static final String TOPIC = "otp-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendOtpEvent(String email, String otp, String type) {
        String payload = String.format(
                "{\"email\":\"%s\",\"otp\":\"%s\",\"type\":\"%s\"}",
                email, otp, type
        );
        kafkaTemplate.send(TOPIC, email, payload);
        log.info("OTP sent to Kafka ✅ email={} type={}", email, type);
    }
}