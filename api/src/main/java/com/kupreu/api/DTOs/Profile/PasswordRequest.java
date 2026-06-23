package com.kupreu.api.DTOs.Profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for changing a password, carrying the current password and the new
 * one. The new password is validated for length and complexity.
 */
@Data
public class PasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
        message = "Password must contain at least one lowercase letter, one uppercase letter, one digit and one special character"
    )
    private String newPassword;

    @NotBlank(message = "Actual password is required")
    private String actualPassword;
}
