package com.kupreu.api.DTOs.UnitOfMeasure;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitOfMeasureResponse {
    public String name;
    public String symbol;
}
