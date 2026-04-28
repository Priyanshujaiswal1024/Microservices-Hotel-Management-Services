package com.hotel.hotel.feignclient;

import com.hotel.hotel.dto.RatingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "RATING-SERVICE")
public interface RatingClient {

    @GetMapping("/api/ratings/hotel/{hotelId}")
    List<RatingResponseDTO> getRatingsByHotelId(@PathVariable Long hotelId);

    @DeleteMapping("/api/ratings/hotel/{hotelId}")
    void deleteRatingsByHotelId(@PathVariable Long hotelId);
}