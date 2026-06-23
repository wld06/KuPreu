package com.kupreu.api.DTOs.Profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload carrying an e-mail used to look up a user's profile.
 */
@Data
public class ProfileRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
