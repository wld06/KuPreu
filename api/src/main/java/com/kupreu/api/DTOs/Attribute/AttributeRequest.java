package com.kupreu.api.DTOs.Attribute;

import lombok.Data;

/**
 * Request payload for creating or updating an attribute.
 */
@Data
public class AttributeRequest {
    private String name;
}
