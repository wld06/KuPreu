package com.kupreu.api.DTOs.Product;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 50, message = "EAN must be at most 50 characters")
    private String ean;

    private int stock;

    @NotNull(message = "Subcategory ID is required")
    private UUID subcategoryId;

    @NotNull(message = "Brand ID is required")
    private UUID brandId;

    @NotNull(message = "Unit of Measure ID is required")
    private UUID unitOfMeasureId;
}
