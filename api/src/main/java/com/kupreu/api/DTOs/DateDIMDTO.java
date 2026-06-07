package com.kupreu.api.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DateDIMDTO {
    public UUID id;
    public LocalDateTime date;
}
