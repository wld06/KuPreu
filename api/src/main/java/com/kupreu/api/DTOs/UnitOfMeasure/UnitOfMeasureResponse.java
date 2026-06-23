package com.kupreu.api.DTOs.UnitOfMeasure;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response payload representing a unit of measure (name and symbol).
 */
@Data
@Builder
public class UnitOfMeasureResponse {
    private UUID id;
    private String name;
    private String symbol;
}
