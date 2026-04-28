package com.hotel.hotel.controller;

import com.hotel.hotel.dto.ApiResponseDTO;
import com.hotel.hotel.dto.HotelRequestDTO;
import com.hotel.hotel.dto.HotelResponseDTO;
import com.hotel.hotel.service.HotelService;
import com.hotel.hotel.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final ImageService imageService;

    // ── ADMIN ONLY ──────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<HotelResponseDTO>> createHotel(
            @Valid @RequestBody HotelRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Hotel created",
                        hotelService.createHotel(request)));
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("File is empty"));
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Only image files allowed"));
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Image uploaded", imageService.uploadImage(file)));
    }

    @PostMapping("/with-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<HotelResponseDTO>> createHotelWithImage(
            @RequestParam("name")        String name,
            @RequestParam("location")    String location,
            @RequestParam("category")    String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image",       required = false) MultipartFile image) {

        String imageUrl = (image != null && !image.isEmpty())
                ? imageService.uploadImage(image) : null;

        HotelRequestDTO req = new HotelRequestDTO();
        req.setName(name); req.setLocation(location);
        req.setCategory(category); req.setDescription(description);
        req.setImageUrl(imageUrl);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Hotel created",
                        hotelService.createHotel(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<HotelResponseDTO>> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotel updated",
                hotelService.updateHotel(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Hotel deleted", null));
    }

    // ── USER + ADMIN ─────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<HotelResponseDTO>>> getAllHotels() {
        return ResponseEntity.ok(ApiResponseDTO.success("All hotels",
                hotelService.getAllHotels()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<HotelResponseDTO>> getHotelById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotel fetched",
                hotelService.getHotelById(id)));
    }

    @GetMapping("/location/{location}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<HotelResponseDTO>>> getByLocation(
            @PathVariable String location) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotels by location",
                hotelService.getHotelsByLocation(location)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<HotelResponseDTO>>> searchHotels(
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponseDTO.success("Search results",
                hotelService.searchHotels(name)));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<HotelResponseDTO>>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponseDTO.success("Hotels by category",
                hotelService.getHotelsByCategory(category)));
    }
}