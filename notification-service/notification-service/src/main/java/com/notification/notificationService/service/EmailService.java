package notificationService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    public void sendRegistrationOtp(String email, String otp) {
        sendEmail(
                email,
                "Verify Your Email — Hotel Booking",
                "Welcome! Your OTP is: " + otp + "\nValid for 5 minutes."
        );
    }
    public void sendForgotPasswordOtp(String email, String otp) {
        sendEmail(
                email,
                "Password Reset OTP — Hotel Booking",
                "Your password reset OTP is: " + otp + "\nValid for 5 minutes."
        );
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.info("Email sent to={}", to);
    }
}
