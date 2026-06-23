package com.kupreu.api.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a date-dimension entry (id plus its date value).
 */
@Data
@Builder
public class DateDIMDTO {
    private UUID id;
    private LocalDateTime date;
}
