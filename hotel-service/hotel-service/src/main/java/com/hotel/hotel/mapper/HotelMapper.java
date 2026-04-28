package com.hotel.hotel.mapper;

import com.hotel.hotel.dto.HotelRequestDTO;
import com.hotel.hotel.dto.HotelResponseDTO;
import com.hotel.hotel.dto.RatingResponseDTO;
import com.hotel.hotel.entity.Hotel;
import com.hotel.hotel.entity.HotelCategory;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class HotelMapper {

    public Hotel toEntity(HotelRequestDTO dto) {
        return Hotel.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .category(dto.getCategory() != null
                        ? HotelCategory.valueOf(dto.getCategory().toUpperCase())
                        : HotelCategory.STANDARD)
                .imageUrl(dto.getImageUrl())
                .pricePerNight(dto.getPricePerNight())  // ← ADD
                .totalRooms(dto.getTotalRooms())        // ← ADD
                .build();
    }

    public HotelResponseDTO toResponseDTO(Hotel hotel,
                                          List<RatingResponseDTO> ratings) {
        double avg = 0.0;
        if (ratings != null && !ratings.isEmpty()) {
            avg = ratings.stream()
                    .mapToInt(RatingResponseDTO::getRating)
                    .average().orElse(0.0);
            avg = Math.round(avg * 10.0) / 10.0;
        }
        return HotelResponseDTO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .category(hotel.getCategory() != null
                        ? hotel.getCategory().name() : null)
                .imageUrl(hotel.getImageUrl())
                .pricePerNight(hotel.getPricePerNight())  // ← ADD
                .totalRooms(hotel.getTotalRooms())        // ← ADD
                .averageRating(avg)
                .totalRatings(ratings != null ? ratings.size() : 0)
                .ratings(ratings)
                .createdAt(hotel.getCreatedAt())
                .build();
    }
}