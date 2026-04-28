package com.Hotel.user.service;

import com.Hotel.user.dto.*;
import com.Hotel.user.entity.User;
import com.Hotel.user.exception.ResourceNotFoundException;
import com.Hotel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Read ────────────────────────────────────────────────────────────────────

    public UserResponseDto getUserById(Long id) {
        return toDto(findOrThrow(id));
    }

    /**
     * Admin-only — returns paginated list of all users.
     * @PreAuthorize is enforced by Spring Security method security.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDto);
    }

    // ── Update ──────────────────────────────────────────────────────────────────

    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = findOrThrow(id);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        // Password is optional — only update if provided
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("User updated: {}", saved.getUsername());
        return toDto(saved);
    }

    // ── Delete ──────────────────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        findOrThrow(id); // ensures 404 if not found
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    private UserResponseDto toDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRoles(user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet()));
        return dto;
    }
}