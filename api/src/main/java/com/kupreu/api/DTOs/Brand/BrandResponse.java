package com.kupreu.api.DTOs.Brand;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response payload representing a brand returned to clients.
 */
@Data
@Builder
public class BrandResponse {
    private UUID id;
    private String name;
}