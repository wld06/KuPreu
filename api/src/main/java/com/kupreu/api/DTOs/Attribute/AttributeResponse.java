package com.kupreu.api.DTOs.Attribute;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttributeResponse {
    public UUID id;
    public String name;
}
