package com.Hotel.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BookingRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in must be today or future")
    private LocalDate checkIn;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out must be a future date")
    private LocalDate checkOut;

    @Min(value = 1, message = "Minimum 1 guest")
    @Max(value = 10, message = "Maximum 10 guests")
    private int guests;
}