package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubcategoryRequest {
    @NotBlank
    private String name;

    @NotNull
    private UUID categoryId;
}
