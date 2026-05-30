package com.kupreu.api.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostalCodeDTO {
    public String code;
    public String city;
}
