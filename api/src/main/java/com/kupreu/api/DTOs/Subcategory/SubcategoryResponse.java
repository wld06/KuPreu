package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a subcategory (without its parent category).
 */
@Data
@Builder
public class SubcategoryResponse {
    private UUID id;
    private String name;
}
