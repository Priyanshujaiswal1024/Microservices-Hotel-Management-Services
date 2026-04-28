package com.Hotel.rating.mapper;

import com.Hotel.rating.dto.RatingRequestDTO;
import com.Hotel.rating.dto.RatingResponseDTO;
import com.Hotel.rating.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public Rating toEntity(RatingRequestDTO dto) {
        return Rating.builder()
                .hotelId(dto.getHotelId())
                .userId(dto.getUserId())
                .rating(dto.getRating())
                .feedback(dto.getFeedback())
                .build();
    }

    public RatingResponseDTO toResponseDTO(Rating rating) {
        return RatingResponseDTO.builder()
                .id(rating.getId())
                .hotelId(rating.getHotelId())
                .userId(rating.getUserId())
                .rating(rating.getRating())
                .feedback(rating.getFeedback())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}