package com.Hotel.booking.controller;

import com.Hotel.booking.dto.*;
import com.Hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ─── CREATE ────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Booking created",
                        bookingService.createBooking(request)));
    }

    // ─── GET ALL ───────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponseDTO.success("All bookings",
                bookingService.getAllBookings()));
    }

    // ─── GET BY ID ─────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> getBookingById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Booking fetched",
                bookingService.getBookingById(id)));
    }

    // ─── GET BY USER
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success("User bookings",
                bookingService.getBookingsByUserId(userId)));
    }

    // ─── GET BY HOTEL ──────────────────────────────────────
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getByHotelId(
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotel bookings",
                bookingService.getBookingsByHotelId(hotelId)));
    }

    // ─── CHECK AVAILABILITY ────────────────────────────────
    @GetMapping("/availability/{hotelId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<AvailabilityResponseDTO>> checkAvailability(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut) {
        return ResponseEntity.ok(ApiResponseDTO.success("Availability checked",
                bookingService.checkAvailability(hotelId, checkIn, checkOut)));
    }

    // ─── UPDATE STATUS (ADMIN) ─────────────────────────────
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponseDTO.success("Status updated",
                bookingService.updateStatus(id, status)));
    }

    // ─── CANCEL ────────────────────────────────────────────
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> cancelBooking(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Booking cancelled",
                bookingService.cancelBooking(id)));
    }
}