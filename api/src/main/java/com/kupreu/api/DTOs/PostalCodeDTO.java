package com.kupreu.api.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostalCodeDTO {
    private String code;
    private String city;
}
