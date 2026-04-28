package com.Hotel.user.service;

import com.Hotel.user.dto.*;
import com.Hotel.user.entity.RoleType;
import com.Hotel.user.entity.User;
import com.Hotel.user.exception.*;
import com.Hotel.user.kafka.OtpEventProducer;
import com.Hotel.user.repository.UserRepository;
import com.Hotel.user.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final OtpService           otpService;
    private final PendingUserCacheService pendingUserCache;
    private final OtpEventProducer     otpEventProducer;
    private final AuthenticationManager authenticationManager;
    private final JWTService           jwtService;

    /**
     * Step 1 — Register intent.
     * Validates uniqueness, caches the DTO in Redis, and sends an OTP.
     * The User row is NOT written to the DB yet.
     */
    @Transactional
    public String signup(SignUpRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + dto.getUsername());
        }

        // Cache pending data so we can create the user after OTP is verified
        pendingUserCache.save(dto.getUsername(), dto);

        String otp = otpService.generateAndSaveOtp(dto.getUsername());
        otpEventProducer.sendOtpEvent(dto.getUsername(), otp, "REGISTRATION");

        return "OTP sent to " + dto.getUsername() + ". Please verify to complete registration.";
    }

    /**
     * Step 2 — Verify OTP.
     * Creates the User row only after the OTP is confirmed valid.
     * Returns a JWT so the user is logged in immediately.
     */
    @Transactional
    public LoginResponseDTO verifyOtp(VerifyOtpRequestDto dto) {
        // Verify OTP first — fail fast before touching the DB
        if (!otpService.verifyAndDelete(dto.getEmail(), dto.getOtp())) {
            throw new InvalidOtpException("Invalid or expired OTP.");
        }

        // Check the user wasn't somehow already created (idempotency guard)
        if (userRepository.existsByUsername(dto.getEmail())) {
            throw new UserAlreadyExistsException("User already verified.");
        }

        // Retrieve the pending signup data from cache
        SignUpRequestDto pending = pendingUserCache.get(dto.getEmail());

        // Now create the verified user in the DB
        User user = User.builder()
                .username(pending.getUsername())
                .password(passwordEncoder.encode(pending.getPassword()))
                .fullName(pending.getFullName())
                .phone(pending.getPhone())
                .emailVerified(true)           // verified from the start
                .roles(Set.of(RoleType.USER))
                .build();

        userRepository.save(user);
        pendingUserCache.delete(dto.getEmail()); // clean up cache

        return new LoginResponseDTO(jwtService.generateToken(user), user.getId());
    }

    /**
     * Resend OTP — only if the user has NOT yet been verified.
     */
    public String resendOtp(String email) {
        // If the user already exists in DB they are verified — no need to resend
        if (userRepository.existsByUsername(email)) {
            throw new UserAlreadyExistsException("Email is already verified. Please log in.");
        }

        // Ensure there is actually a pending signup for this email
        pendingUserCache.get(email); // throws if session expired

        String otp = otpService.generateAndSaveOtp(email);
        otpEventProducer.sendOtpEvent(email, otp, "REGISTRATION");
        return "OTP resent to " + email;
    }

    /**
     * Login — authenticate, confirm email is verified, return JWT.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        // Throws BadCredentialsException (401) automatically if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(), dto.getPassword())
        );

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + dto.getUsername()));

        // isEnabled() in UserPrincipal already guards this, but being explicit is fine
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in.");
        }

        return new LoginResponseDTO(jwtService.generateToken(user), user.getId());
    }
}