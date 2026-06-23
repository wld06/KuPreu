package com.kupreu.api.DTOs;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned after a successful registration or login,
 * carrying the issued JWT and the authenticated user's basic details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String username;
    private UUID id;
}
