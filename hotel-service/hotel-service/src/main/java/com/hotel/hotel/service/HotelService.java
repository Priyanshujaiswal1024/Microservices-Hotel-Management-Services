package com.hotel.hotel.service;

import com.hotel.hotel.entity.HotelCategory;
import com.hotel.hotel.repository.HotelRepository;
import com.hotel.hotel.dto.HotelRequestDTO;
import com.hotel.hotel.dto.HotelResponseDTO;
import com.hotel.hotel.dto.RatingResponseDTO;
import com.hotel.hotel.entity.Hotel;
import com.hotel.hotel.exception.ResourceNotFoundException;
import com.hotel.hotel.feignclient.RatingClient;
import com.hotel.hotel.mapper.HotelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RatingClient ratingClient;
    private final HotelMapper hotelMapper;

    public HotelResponseDTO createHotel(HotelRequestDTO request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel created: {}", saved.getName());
        return hotelMapper.toResponseDTO(saved, Collections.emptyList());
    }

    public HotelResponseDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
        List<RatingResponseDTO> ratings = fetchRatings(id);
        return hotelMapper.toResponseDTO(hotel, ratings);
    }

    public List<HotelResponseDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotel -> hotelMapper.toResponseDTO(hotel, fetchRatings(hotel.getId())))
                .collect(Collectors.toList());
    }

    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found: " + id));
        hotel.setName(request.getName());
        hotel.setLocation(request.getLocation());
        hotel.setDescription(request.getDescription());
        hotel.setImageUrl(request.getImageUrl());
        hotel.setPricePerNight(request.getPricePerNight());
        hotel.setTotalRooms(request.getTotalRooms());
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponseDTO(saved, fetchRatings(id));
    }
    public void deleteHotel(Long id) {
        hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
        hotelRepository.deleteById(id);
        // Rating Service se bhi delete karo
        try {
            ratingClient.deleteRatingsByHotelId(id);
        } catch (Exception e) {
            log.warn("Could not delete ratings for hotel {}: {}", id, e.getMessage());
        }
    }

    public List<HotelResponseDTO> getHotelsByLocation(String location) {
        return hotelRepository.findByLocation(location).stream()
                .map(hotel -> hotelMapper.toResponseDTO(hotel, fetchRatings(hotel.getId())))
                .collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchHotels(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name).stream()
                .map(hotel -> hotelMapper.toResponseDTO(hotel, fetchRatings(hotel.getId())))
                .collect(Collectors.toList());
    }
    public List<HotelResponseDTO> getHotelsByCategory(String category) {
        HotelCategory hotelCategory = HotelCategory.valueOf(category.toUpperCase()); // ← convert
        return hotelRepository.findByCategory(hotelCategory).stream()
                .map(hotel -> hotelMapper.toResponseDTO(hotel, fetchRatings(hotel.getId())))
                .collect(Collectors.toList());
    }

    // Private helper — Rating Service unavailable hone pe empty list return
    private List<RatingResponseDTO> fetchRatings(Long hotelId) {
        try {
            return ratingClient.getRatingsByHotelId(hotelId);
        } catch (Exception e) {
            log.warn("Rating service unavailable for hotel {}: {}", hotelId, e.getMessage());
            return Collections.emptyList();
        }
    }
}