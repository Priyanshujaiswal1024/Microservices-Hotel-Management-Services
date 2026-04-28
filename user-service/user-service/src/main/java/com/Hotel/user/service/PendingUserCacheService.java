package com.Hotel.user.service;

import com.Hotel.user.dto.SignUpRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Stores a pending (unverified) signup in Redis so the User row
 * is only written to the DB AFTER OTP verification succeeds.
 */
@Service
@RequiredArgsConstructor
public class PendingUserCacheService {

    private static final String  PREFIX  = "pending_user:";
    private static final int     TTL_MIN = 15; // slightly longer than OTP TTL

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;

    public void save(String email, SignUpRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue()
                    .set(PREFIX + email, json, Duration.ofMinutes(TTL_MIN));
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache pending user", e);
        }
    }

    public SignUpRequestDto get(String email) {
        String json = redisTemplate.opsForValue().get(PREFIX + email);
        if (json == null) {
            throw new RuntimeException("Signup session expired. Please sign up again.");
        }
        try {
            return objectMapper.readValue(json, SignUpRequestDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read pending user", e);
        }
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}