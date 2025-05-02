package com.lasha.tasktracker.rest;

import com.lasha.tasktracker.dto.CreateUserRequest;
import com.lasha.tasktracker.entity.UserEntity;
import com.lasha.tasktracker.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: email is already taken");
        }

        UserEntity user = UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("User with role " + req.getRole() + " created successfully");
    }
}
