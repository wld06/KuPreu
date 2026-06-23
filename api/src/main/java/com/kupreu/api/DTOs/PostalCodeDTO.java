package com.kupreu.api.DTOs;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a postal code and its city.
 */
@Data
@Builder
public class PostalCodeDTO {
    private String code;
    private String city;
}
