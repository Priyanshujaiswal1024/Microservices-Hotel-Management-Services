
package com.hotel.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotels")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private HotelCategory category;

    private String imageUrl;

    @Column(nullable = false)
    private Double pricePerNight;

    private Integer totalRooms;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}