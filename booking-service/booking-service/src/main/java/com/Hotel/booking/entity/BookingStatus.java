package com.Hotel.booking.entity;

import jakarta.persistence.*;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}