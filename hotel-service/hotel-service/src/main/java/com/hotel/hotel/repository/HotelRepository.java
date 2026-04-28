package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Hotel;
import com.hotel.hotel.entity.HotelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByLocation(String location);
    List<Hotel> findByCategory(HotelCategory category);
    List<Hotel> findByNameContainingIgnoreCase(String name);
}