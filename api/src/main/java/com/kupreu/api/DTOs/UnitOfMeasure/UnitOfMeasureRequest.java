package com.kupreu.api.DTOs.UnitOfMeasure;

import lombok.Data;

/**
 * Request payload for creating or updating a unit of measure (name and symbol).
 */
@Data
public class UnitOfMeasureRequest {
    private String name;
    private String symbol;
}
