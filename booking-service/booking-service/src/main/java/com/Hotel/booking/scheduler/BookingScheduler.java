package com.Hotel.booking.scheduler;

import com.Hotel.booking.entity.Booking;
import com.Hotel.booking.entity.BookingStatus;
import com.Hotel.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    // Har 1 minute mein check karo
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelPendingBookings() {
        // 15 minute se zyada purani PENDING bookings cancel karo
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<Booking> expiredBookings = bookingRepository
                .findExpiredPendingBookings(cutoff);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Auto-cancelled expired booking: id={}", booking.getId());
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Auto-cancelled {} expired bookings", expiredBookings.size());
        }
    }
}