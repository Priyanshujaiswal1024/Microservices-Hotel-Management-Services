package com.Hotel.rating.service;

import com.Hotel.rating.RatingRepository;
import com.Hotel.rating.dto.RatingRequestDTO;
import com.Hotel.rating.dto.RatingResponseDTO;
import com.Hotel.rating.entity.Rating;
import com.Hotel.rating.exception.ResourceNotFoundException; // make sure this exists
import com.Hotel.rating.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public RatingResponseDTO createRating(RatingRequestDTO request) {
        Rating rating = ratingMapper.toEntity(request);
        Rating saved = ratingRepository.save(rating);
        log.info("Rating created for hotel: {}", saved.getHotelId());
        return ratingMapper.toResponseDTO(saved);
    }

    public RatingResponseDTO getRatingById(Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found: " + id));
        return ratingMapper.toResponseDTO(rating);
    }

    public List<RatingResponseDTO> getRatingsByHotelId(Long hotelId) {
        return ratingRepository.findByHotelId(hotelId)
                .stream().map(ratingMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<RatingResponseDTO> getRatingsByUserId(Long userId) {
        return ratingRepository.findByUserId(userId)
                .stream().map(ratingMapper::toResponseDTO).collect(Collectors.toList());
    }

    public List<RatingResponseDTO> getAllRatings() {
        return ratingRepository.findAll()
                .stream().map(ratingMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public RatingResponseDTO updateRating(Long id, RatingRequestDTO request) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found: " + id));
        rating.setRating(request.getRating());
        rating.setFeedback(request.getFeedback());
        return ratingMapper.toResponseDTO(ratingRepository.save(rating));
    }

    public void deleteRating(Long id) {
        ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found: " + id));
        ratingRepository.deleteById(id);
    }

    @Transactional
    public void deleteRatingsByHotelId(Long hotelId) {
        ratingRepository.deleteByHotelId(hotelId);
        log.info("All ratings deleted for hotel: {}", hotelId);
    }
}