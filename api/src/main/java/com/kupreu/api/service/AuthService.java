package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.ConflictException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import com.kupreu.api.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.AuthResponse;
import com.kupreu.api.DTOs.LoginRequest;
import com.kupreu.api.DTOs.RegisterRequest;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())){
            throw new ConflictException("Username already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .name(request.getName())
                .surname(request.getSurname())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAdmin(false)
                .build();

        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getEmail());

        auditService.record("USER_REGISTERED", user.getEmail(),
                "New user registered", null, true);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .id(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            auditService.record("LOGIN_FAILED", request.getEmail(),
                    "Failed login attempt", ex.getClass().getSimpleName(), false);
            throw ex;
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String token = jwtProvider.generateToken(user.getEmail());

        auditService.record("LOGIN_SUCCESS", user.getEmail(),
                "User logged in", null, true);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .username(user.getUsername())
                .id(user.getId())
                .build();
    }
}
