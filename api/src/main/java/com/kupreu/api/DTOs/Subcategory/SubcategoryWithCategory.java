package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import com.kupreu.api.DTOs.Category.CategoryResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubcategoryWithCategory {
    public UUID id;
    public String name;
    public CategoryResponse category;
}