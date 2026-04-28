package com.Hotel.booking.feignClient;

import com.Hotel.booking.dto.HotelResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "HOTEL-SERVICE")
public interface HotelClient {

    @GetMapping("/api/hotels/{id}")
    HotelResponseDTO getHotelById(@PathVariable Long id);
}