package notificationService.kafka.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.notification.notificationService.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import notificationService.service.EmailService;
import notificationService.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    // containerFactory nahi — simple String listener
    @KafkaListener(topics = "otp-events", groupId = "notification-group")
    public void handleOtpEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String email  = node.get("email").asText();
            String otp    = node.get("otp").asText();
            String type   = node.get("type").asText();

            log.info("Kafka event received email={} type={}", email, type);

            switch (type) {
                case "REGISTRATION" ->
                        emailService.sendRegistrationOtp(email, otp);
                case "FORGOT_PASSWORD" ->
                        emailService.sendForgotPasswordOtp(email, otp);
                default ->
                        log.warn("Unknown event type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to process OTP event: {}", e.getMessage());
        }
    }
}