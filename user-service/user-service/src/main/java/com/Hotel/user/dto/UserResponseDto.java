package com.Hotel.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long        id;
    private String      username;
    private String      fullName;
    private String      phone;
    private boolean     emailVerified;
    private Set<String> roles;
}