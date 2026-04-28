package com.Hotel.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequestDto {

    // email/username intentionally not updatable here
    private String fullName;
    private String phone;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password; // optional — only updated if provided
}