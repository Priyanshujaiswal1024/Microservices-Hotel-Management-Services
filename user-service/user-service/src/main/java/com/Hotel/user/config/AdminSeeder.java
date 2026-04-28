package com.Hotel.user.config;

import com.Hotel.user.entity.RoleType;
import com.Hotel.user.entity.User;
import com.Hotel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            String adminEmail = "admin@hotel.com";

            if (userRepository.findByUsername(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .username(adminEmail)
                        .password(passwordEncoder.encode("Admin@123"))
                        .fullName("Super Admin")
                        .phone("9999999999")
                        .emailVerified(true)        // ← seedeed admin verified hai
                        .roles(Set.of(RoleType.ADMIN))
                        .build();

                userRepository.save(admin);
                log.info("✅ Admin seeded: email={} password=Admin@123", adminEmail);
            } else {
                log.info("ℹ️ Admin already exists — skipping seed");
            }
        };
    }
}