package com.kupreu.api.DTOs.ShoppingList;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItemRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "The quantity has to be more or equal than 1")
    private Integer quantity;

    @NotNull(message = "Price snapshot uuid is required")
    private UUID priceSnapshotUuid;
}
