package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for creating or updating a subcategory, referencing its parent category.
 */
@Data
public class SubcategoryRequest {
    @NotBlank
    private String name;

    @NotNull
    private UUID categoryId;
}
