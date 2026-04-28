package com.Hotel.booking.mapper;

import com.Hotel.booking.dto.*;
import com.Hotel.booking.entity.Booking;
import org.springframework.stereotype.Component;
import java.time.temporal.ChronoUnit;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequestDTO dto) {
        return Booking.builder()
                .userId(dto.getUserId())
                .hotelId(dto.getHotelId())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .guests(dto.getGuests())
                .build();
    }

    public BookingResponseDTO toResponseDTO(Booking booking,
                                            UserResponseDTO user,
                                            HotelResponseDTO hotel) {
        int nights = (int) ChronoUnit.DAYS.between(
                booking.getCheckIn(), booking.getCheckOut());

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .nights(nights)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .bookedAt(booking.getBookedAt())
                .updatedAt(booking.getUpdatedAt())
                .user(user)
                .hotel(hotel)
                .build();
    }
}