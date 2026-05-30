package com.kupreu.api.DTOs.Category;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
}
