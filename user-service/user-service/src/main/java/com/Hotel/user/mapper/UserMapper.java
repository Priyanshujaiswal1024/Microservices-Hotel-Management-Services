package com.Hotel.user.mapper;

import com.Hotel.user.dto.*;
import com.Hotel.user.entity.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(RegisterRequestDTO dto) {
        return User.builder()
                .username(dto.getEmail())
                .fullName(dto.getName())
                .roles(Set.of(dto.getRole() != null && dto.getRole().equals("ADMIN")
                        ? RoleType.ADMIN : RoleType.USER))
                .build();
    }

    public UserResponseDto toResponseDTO(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }
}