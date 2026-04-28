package com.Hotel.user.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String jwt;
    private Long userId;
}