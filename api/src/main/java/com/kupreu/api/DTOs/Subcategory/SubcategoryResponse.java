package com.kupreu.api.DTOs.Subcategory;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubcategoryResponse {
    private UUID id;
    private String name;
}
