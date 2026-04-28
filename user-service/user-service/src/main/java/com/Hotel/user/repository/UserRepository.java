package com.Hotel.user.repository;

import com.Hotel.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByPhone(String phone);

    boolean existsByUsername(@NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String username);
}