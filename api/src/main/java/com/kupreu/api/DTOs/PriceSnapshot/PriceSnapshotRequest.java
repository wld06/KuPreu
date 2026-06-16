package com.kupreu.api.DTOs.PriceSnapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PriceSnapshotRequest {
    @NotNull(message = "Product id is required")
    private UUID productId;

    @NotNull(message = "Store id is required")
    private UUID storeId;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Start date is required")
    private LocalDateTime dateStart;

    // Null means the price is still current
    private LocalDateTime dateEnd;
}
