package com.hotel.hotel.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HotelResponseDTO {
    private Long id;
    private String name;
    private String location;
    private String description;
    private String category;
    private String imageUrl;
    private Double pricePerNight;   // ← ADD
    private Integer totalRooms;     // ← ADD
    private Double averageRating;
    private Integer totalRatings;
    private List<RatingResponseDTO> ratings;
    private LocalDateTime createdAt;
}