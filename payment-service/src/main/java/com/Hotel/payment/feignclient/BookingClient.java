package com.Hotel.payment.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingClient {

    @PutMapping("/api/bookings/{id}/status")
    void updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status);
}