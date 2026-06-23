package com.kupreu.api.DTOs.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating or updating a category, with bean-validation constraints.
 */
@Data
public class CategoryRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;
}
