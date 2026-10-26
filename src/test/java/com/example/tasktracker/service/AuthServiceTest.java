package com.example.tasktracker.service;

import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
    }
    @Test
    void register_hashesPasswordBeforeSavingUser(){
        String username = "test_user";
        String password = "StrongPass123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AppUser savedUser = authService.register(username,password);
        String hash = savedUser.getPasswordHash();
        assertTrue(passwordEncoder.matches(password,hash));
        assertFalse(hash.equals(password));

        assertEquals(username,savedUser.getUsername());
        assertEquals("USER",savedUser.getRole());

        verify(userRepository).save(any(AppUser.class));
    }
}