package com.Hotel.user.controller;

import com.Hotel.user.dto.*;
import com.Hotel.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequestDto dto) {
        return ResponseEntity.ok(authService.signup(dto));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponseDTO> verifyOtp(
            @RequestBody VerifyOtpRequestDto dto) {
        return ResponseEntity.ok(authService.verifyOtp(dto));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendOtp(email));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}