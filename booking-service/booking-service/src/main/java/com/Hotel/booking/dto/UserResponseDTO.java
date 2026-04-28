package com.Hotel.booking.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String phone;
}