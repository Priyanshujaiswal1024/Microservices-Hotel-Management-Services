// booking-service/dto/HotelResponseDTO.java
package com.Hotel.booking.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HotelResponseDTO {
    private Long id;
    private String name;
    private String location;
    private String category;
    private Double pricePerNight;  // ← ADD
    private Integer totalRooms;    // ← ADD
    private Double averageRating;
}