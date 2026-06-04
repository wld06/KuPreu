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
    public String name;

    @Size(max = 50, message = "EAN must be at most 50 characters")
    public String ean;

    public int stock;

    @NotNull(message = "Subcategory ID is required")
    public UUID subcategoryId;

    @NotNull(message = "Brand ID is required")
    public UUID brandId;

    @NotNull(message = "Unit of Measure ID is required")
    public UUID unitOfMeasureId;
}
