package com.kupreu.api.DTOs.Category;

import java.util.List;
import java.util.UUID;

import com.kupreu.api.DTOs.Subcategory.SubcategoryResponse;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a category together with its subcategories.
 */
@Data
@Builder
public class CategoryWithSubcategoriesResponse {
    private UUID id;
    private String name;
    private List<SubcategoryResponse> subcategories;
}
