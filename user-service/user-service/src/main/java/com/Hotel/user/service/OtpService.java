package com.Hotel.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    public String generateAndSaveOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        redisTemplate.opsForValue()
                .set("otp:" + email, otp, Duration.ofMinutes(5));
        return otp;
    }

    public boolean verifyAndDelete(String email, String otp) {
        String stored = redisTemplate.opsForValue().get("otp:" + email);
        if (stored != null && stored.equals(otp)) {
            redisTemplate.delete("otp:" + email);
            return true;
        }
        return false;
    }
}