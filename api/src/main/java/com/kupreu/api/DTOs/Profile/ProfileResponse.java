package com.kupreu.api.DTOs.Profile;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kupreu.api.DTOs.PostalCodeDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    public UUID id;
    public String username;
    public String name;
    public String surname;
    public String email;
    public PostalCodeDTO postalCode;
    public LocalDateTime createdAt;
}
