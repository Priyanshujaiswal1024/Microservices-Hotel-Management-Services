package com.hotel.hotel.dto;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RatingResponseDTO {
    private Long id;
    private Long hotelId;
    private Long userId;
    private int rating;
    private String feedback;
    private LocalDateTime createdAt;
}