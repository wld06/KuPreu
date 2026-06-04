package com.kupreu.api.DTOs.Product;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    public UUID id;
    public String ean;
    public String name;
    public int stockQty;
    public String subcategoryName;
    public String brandName;
    public String unitOfMeasureName;
}
