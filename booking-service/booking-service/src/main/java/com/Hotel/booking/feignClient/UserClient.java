package com.Hotel.booking.feignClient;

import com.Hotel.booking.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponseDTO getUserById(@PathVariable Long id);
}