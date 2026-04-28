package com.Hotel.rating.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RatingRequestDTO {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Min(value = 1, message = "Rating min 1")
    @Max(value = 5, message = "Rating max 5")
    private int rating;

    @Size(max = 500, message = "Feedback max 500 characters")
    private String feedback;
}