package com.kupreu.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.AuthResponse;
import com.kupreu.api.DTOs.LoginRequest;
import com.kupreu.api.DTOs.RegisterRequest;
import com.kupreu.api.service.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing public authentication endpoints under {@code /api/auth}:
 * user registration and login. Both return a JWT on success.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @param request the validated registration data
     * @return HTTP 200 with the issued token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticates a user and issues a token.
     *
     * @param request the validated login credentials
     * @return HTTP 200 with the issued token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
