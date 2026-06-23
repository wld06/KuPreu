package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import com.kupreu.api.DTOs.Category.CategoryResponse;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a subcategory together with its parent category.
 */
@Data
@Builder
public class SubcategoryWithCategory {
    private UUID id;
    private String name;
    private CategoryResponse category;
}