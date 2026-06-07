package com.kupreu.api.DTOs.PriceSnapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PriceSnapshotRequest {
    public UUID productId;
    public UUID storeId;
    public BigDecimal price;
    public LocalDateTime dateStart;
    public LocalDateTime dateEnd;
}
