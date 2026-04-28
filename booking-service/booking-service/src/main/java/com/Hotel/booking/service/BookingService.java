package com.Hotel.booking.service;

import com.Hotel.booking.dto.*;
import com.Hotel.booking.entity.*;
import com.Hotel.booking.exception.*;
import com.Hotel.booking.feignClient.HotelClient;
import com.Hotel.booking.feignClient.UserClient;
import com.Hotel.booking.mapper.BookingMapper;
import com.Hotel.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserClient        userClient;
    private final HotelClient       hotelClient;
    private final BookingMapper     bookingMapper;


    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {

        // Step 1 — Date validation
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new InvalidBookingException(
                    "Check-out must be after check-in date");
        }

        // Step 2 — Minimum 1 night
        long nights = ChronoUnit.DAYS.between(
                request.getCheckIn(), request.getCheckOut());
        if (nights < 1) {
            throw new InvalidBookingException("Minimum booking is 1 night");
        }

        // Step 3 — Double Booking Check
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
                request.getHotelId(),
                request.getCheckIn(),
                request.getCheckOut()
        );

        if (isOverlapping) {
            List<Booking> activeBookings = bookingRepository
                    .findActiveBookingsByHotelId(
                            request.getHotelId(), LocalDate.now());

            String bookedDates = activeBookings.stream()
                    .map(b -> b.getCheckIn() + " to " + b.getCheckOut())
                    .collect(Collectors.joining(", "));

            throw new DoubleBookingException(
                    "Hotel is not available for selected dates. " +
                            "Already booked: " + bookedDates);
        }

        // Step 4 — User exist karta hai?
        UserResponseDTO user = fetchUser(request.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found: " + request.getUserId());
        }

        // Step 5 — Hotel exist karta hai?
        HotelResponseDTO hotel = fetchHotel(request.getHotelId());
        if (hotel == null) {
            throw new ResourceNotFoundException(
                    "Hotel not found: " + request.getHotelId());
        }

        // Step 6 — Price hotel se lo
        double pricePerNight = (hotel.getPricePerNight() != null
                && hotel.getPricePerNight() > 0)
                ? hotel.getPricePerNight()
                : 2000.0; // fallback

        double totalPrice = nights * pricePerNight * request.getGuests();

        // Step 7 — Save karo
        Booking booking = bookingMapper.toEntity(request);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: id={} hotel={} user={} nights={} " +
                        "pricePerNight={} total={}",
                saved.getId(), saved.getHotelId(), saved.getUserId(),
                nights, pricePerNight, totalPrice);

        return bookingMapper.toResponseDTO(saved, user, hotel);
    }


    // GET BOOKING BY ID

    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = findOrThrow(id);
        return bookingMapper.toResponseDTO(
                booking,
                fetchUser(booking.getUserId()),
                fetchHotel(booking.getHotelId()));
    }

    // GET ALL BOOKINGS

    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(b -> bookingMapper.toResponseDTO(
                        b, fetchUser(b.getUserId()),
                        fetchHotel(b.getHotelId())))
                .collect(Collectors.toList());
    }


    // GET BY USER ID

    public List<BookingResponseDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(b -> bookingMapper.toResponseDTO(
                        b, fetchUser(b.getUserId()),
                        fetchHotel(b.getHotelId())))
                .collect(Collectors.toList());
    }

    // GET BY HOTEL ID

    public List<BookingResponseDTO> getBookingsByHotelId(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId).stream()
                .map(b -> bookingMapper.toResponseDTO(
                        b, fetchUser(b.getUserId()),
                        fetchHotel(b.getHotelId())))
                .collect(Collectors.toList());
    }


    // CHECK AVAILABILITY

    public AvailabilityResponseDTO checkAvailability(Long hotelId,
                                                     LocalDate checkIn,
                                                     LocalDate checkOut) {
        boolean overlapping = bookingRepository.existsOverlappingBooking(
                hotelId, checkIn, checkOut);

        List<Booking> activeBookings = bookingRepository
                .findActiveBookingsByHotelId(hotelId, LocalDate.now());

        List<AvailabilityResponseDTO.BookedDateRangeDTO> bookedDates =
                activeBookings.stream()
                        .map(b -> AvailabilityResponseDTO.BookedDateRangeDTO
                                .builder()
                                .checkIn(b.getCheckIn())
                                .checkOut(b.getCheckOut())
                                .build())
                        .collect(Collectors.toList());

        return AvailabilityResponseDTO.builder()
                .hotelId(hotelId)
                .available(!overlapping)
                .message(overlapping
                        ? "Hotel not available for selected dates"
                        : "Hotel is available!")
                .bookedDates(bookedDates)
                .build();
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━
    // UPDATE STATUS (ADMIN)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━
    @Transactional
    public BookingResponseDTO updateStatus(Long id, String status) {
        Booking booking = findOrThrow(id);
        try {
            booking.setStatus(BookingStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidBookingException(
                    "Invalid status: " + status +
                            ". Valid: PENDING, CONFIRMED, CANCELLED, COMPLETED");
        }
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} status → {}", id, status);
        return bookingMapper.toResponseDTO(
                saved,
                fetchUser(saved.getUserId()),
                fetchHotel(saved.getHotelId()));
    }

    // ━━━━━━━━━━━━━━━━━━━━━
    // CANCEL BOOKING
    // ━━━━━━━━━━━━━━━━━━━━━
    @Transactional
    public BookingResponseDTO cancelBooking(Long id) {
        Booking booking = findOrThrow(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException(
                    "Completed booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled", id);
        return bookingMapper.toResponseDTO(
                saved,
                fetchUser(saved.getUserId()),
                fetchHotel(saved.getHotelId()));
    }

    // ━━━━━━━━━━━━━━━━━━━━━
    // PRIVATE HELPERS
    // ━━━━━━━━━━━━━━━━━━━━━
    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found: " + id));
    }

    private UserResponseDTO fetchUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (Exception e) {
            log.warn("User service unavailable: {}", e.getMessage());
            return null;
        }
    }

    private HotelResponseDTO fetchHotel(Long hotelId) {
        try {
            return hotelClient.getHotelById(hotelId);
        } catch (Exception e) {
            log.warn("Hotel service unavailable: {}", e.getMessage());
            return null;
        }
    }
}