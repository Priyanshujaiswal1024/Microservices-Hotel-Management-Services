package com.hotel.hotel.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HotelRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;
    private String category;
    private String imageUrl;

    @NotNull(message = "Price per night is required")
    @Min(value = 1, message = "Price must be greater than 0")
    private Double pricePerNight;  // ← ADD

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;
}