package com.lasha.tasktracker.rest;

import com.lasha.tasktracker.dto.CreateUserRequest;
import com.lasha.tasktracker.entity.UserEntity;
import com.lasha.tasktracker.enums.Role;
import com.lasha.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminController controller;

    private CreateUserRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("pass123");
        request.setRole(Role.MANAGER);
    }

    @Test
    void createUser_emailTaken_returnsBadRequest() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        ResponseEntity<?> response = controller.createUser(request);

        verify(userRepository).existsByEmail("test@example.com");
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error: email is already taken", response.getBody());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUser_success_savesUserAndReturnsOk() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");

        ResponseEntity<?> response = controller.createUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertEquals("test@example.com", saved.getEmail());
        assertEquals("encodedPass", saved.getPassword());
        assertEquals(Role.MANAGER, saved.getRole());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User with role MANAGER created successfully", response.getBody());
    }
}
