package com.Hotel.booking.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AvailabilityResponseDTO {
    private Long hotelId;
    private boolean available;
    private String message;
    private List<BookedDateRangeDTO> bookedDates;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class BookedDateRangeDTO {
        private LocalDate checkIn;
        private LocalDate checkOut;
    }
}