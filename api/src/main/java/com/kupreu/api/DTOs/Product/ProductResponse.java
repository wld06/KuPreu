package com.kupreu.api.DTOs.Product;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String ean;
    private String name;
    private int stockQty;
    private String subcategoryName;
    private String brandName;
    private String unitOfMeasureName;
}
