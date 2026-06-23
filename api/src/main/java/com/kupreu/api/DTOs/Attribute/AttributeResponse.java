package com.kupreu.api.DTOs.Attribute;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response payload representing an attribute returned to clients.
 */
@Data
@Builder
public class AttributeResponse {
    private UUID id;
    private String name;
}
