package com.Hotel.rating.controller;

import com.Hotel.rating.dto.ApiResponseDTO;
import com.Hotel.rating.dto.RatingRequestDTO;
import com.Hotel.rating.dto.RatingResponseDTO;
import com.Hotel.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDTO<RatingResponseDTO>> createRating(
            @Valid @RequestBody RatingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Rating added", ratingService.createRating(request)));
    }

    @GetMapping("/{id}")

    public ResponseEntity<ApiResponseDTO<RatingResponseDTO>> getRatingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Rating fetched", ratingService.getRatingById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<RatingResponseDTO>>> getAllRatings() {
        return ResponseEntity.ok(ApiResponseDTO.success("All ratings", ratingService.getAllRatings()));
    }
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<RatingResponseDTO>>> getByHotelId(
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotel ratings", ratingService.getRatingsByHotelId(hotelId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<RatingResponseDTO>>> getByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponseDTO.success("User ratings", ratingService.getRatingsByUserId(userId)));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDTO<RatingResponseDTO>> updateRating(
            @PathVariable Long id, @Valid @RequestBody RatingRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Rating updated", ratingService.updateRating(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Rating deleted", null));
    }

    @DeleteMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteByHotelId(@PathVariable Long hotelId) {
        ratingService.deleteRatingsByHotelId(hotelId);
        return ResponseEntity.ok(ApiResponseDTO.success("All ratings deleted for hotel", null));
    }
}