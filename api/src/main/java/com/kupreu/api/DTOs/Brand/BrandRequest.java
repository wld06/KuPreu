package com.kupreu.api.DTOs.Brand;

import lombok.Data;

/**
 * Request payload for creating or updating a brand.
 */
@Data
public class BrandRequest {
    private String name;
}
