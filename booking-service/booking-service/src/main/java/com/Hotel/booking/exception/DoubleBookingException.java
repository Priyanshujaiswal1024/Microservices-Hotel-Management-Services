package com.Hotel.booking.exception;

public class DoubleBookingException extends RuntimeException {
    public DoubleBookingException(String message) { super(message); }
}