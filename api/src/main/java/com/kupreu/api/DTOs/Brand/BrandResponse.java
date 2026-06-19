package com.kupreu.api.DTOs.Brand;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BrandResponse {
    private UUID id;
    private String name;
}