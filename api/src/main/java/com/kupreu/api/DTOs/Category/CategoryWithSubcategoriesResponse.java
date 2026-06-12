package com.kupreu.api.DTOs.Category;

import java.util.List;
import java.util.UUID;

import com.kupreu.api.DTOs.Subcategory.SubcategoryResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryWithSubcategoriesResponse {
    public UUID id;
    public String name;
    public List<SubcategoryResponse> subcategories;
}
