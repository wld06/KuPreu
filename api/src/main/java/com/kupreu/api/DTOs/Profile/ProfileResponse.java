package com.kupreu.api.DTOs.Profile;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kupreu.api.DTOs.PostalCodeDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private UUID id;
    private String username;
    private String name;
    private String surname;
    private String email;
    private PostalCodeDTO postalCode;
    private LocalDateTime createdAt;
}
