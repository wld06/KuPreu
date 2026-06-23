package com.kupreu.api.DTOs.ShoppingList;

import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response payload representing a shopping-list item: its quantity and the associated price snapshot.
 */
@Data
@Builder
public class ShoppingListItemResponse {
    private UUID id;
    private Integer quantity;
    private PriceSnapshotResponse priceSnapshot;
}
