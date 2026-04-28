package com.Hotel.booking.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;
    private Long userId;
    private Long hotelId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int nights;
    private double pricePerNight;
    private double totalPrice;
    private String status;
    private LocalDateTime bookedAt;
    private LocalDateTime updatedAt;

    // FeignClient se enriched
    private UserResponseDTO user;
    private HotelResponseDTO hotel;
}