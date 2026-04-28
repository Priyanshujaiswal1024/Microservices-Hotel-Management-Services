package com.Hotel.booking.repository;

import com.Hotel.booking.entity.Booking;
import com.Hotel.booking.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    List<Booking> findByHotelId(Long hotelId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DOUBLE BOOKING CHECK — Core Query
    // Koi ACTIVE booking overlap karti hai?
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.hotelId = :hotelId
        AND b.status NOT IN ('CANCELLED', 'COMPLETED')
        AND b.checkIn < :checkOut
        AND b.checkOut > :checkIn
    """)
    boolean existsOverlappingBooking(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // Update ke waqt apni booking exclude karo
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.hotelId = :hotelId
        AND b.id != :excludeId
        AND b.status NOT IN ('CANCELLED', 'COMPLETED')
        AND b.checkIn < :checkOut
        AND b.checkOut > :checkIn
    """)
    boolean existsOverlappingBookingExcluding(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeId") Long excludeId
    );

    // Pessimistic Lock — concurrent requests handle karo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);

    // Hotel ki available dates check karo
    @Query("""
        SELECT b FROM Booking b
        WHERE b.hotelId = :hotelId
        AND b.status NOT IN ('CANCELLED', 'COMPLETED')
        AND b.checkOut > :today
        ORDER BY b.checkIn ASC
    """)
    List<Booking> findActiveBookingsByHotelId(
            @Param("hotelId") Long hotelId,
            @Param("today") LocalDate today
    );
    // Existing repository mein ye add karo
    @Query("""
    SELECT b FROM Booking b
    WHERE b.status = 'PENDING'
    AND b.bookedAt < :cutoff
""")
    List<Booking> findExpiredPendingBookings(
            @Param("cutoff") LocalDateTime cutoff);
}