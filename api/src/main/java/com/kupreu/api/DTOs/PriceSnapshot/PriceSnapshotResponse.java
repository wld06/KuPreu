package com.kupreu.api.DTOs.PriceSnapshot;

import java.math.BigDecimal;
import java.util.UUID;

import com.kupreu.api.DTOs.DateDIMDTO;
import com.kupreu.api.DTOs.Store.StoreResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceSnapshotResponse {
    public UUID id;
    public StoreResponse store;
    public BigDecimal price;
    public DateDIMDTO dateStart;
    public DateDIMDTO dateEnd;
}