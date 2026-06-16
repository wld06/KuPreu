package com.kupreu.api.DTOs.ShoppingList;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class ShoppingListItemRequest {
    @NonNull
    @Min(value = 1, message = "The quantity has to be more or equal than 1")
    private Integer quantity;

    @NonNull
    private UUID priceSnapshotUuid;
}
