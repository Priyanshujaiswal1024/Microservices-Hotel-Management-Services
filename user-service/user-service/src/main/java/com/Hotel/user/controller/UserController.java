package com.Hotel.user.controller;

import com.Hotel.user.dto.*;
import com.Hotel.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── GET /api/users/{id} ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserResponseDto>> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponseDTO.success("User fetched", userService.getUserById(id)));
    }

    // ── GET /api/users?page=0&size=20&sort=id,asc  (ADMIN only) ───────────────
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<UserResponseDto>>> getAllUsers(
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponseDTO.success("Users fetched", userService.getAllUsers(pageable)));
    }

    // ── PUT /api/users/{id} ────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(
                ApiResponseDTO.success("User updated", userService.updateUser(id, request)));
    }

    // ── DELETE /api/users/{id}  (ADMIN only) ──────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User deleted", null));
    }
}