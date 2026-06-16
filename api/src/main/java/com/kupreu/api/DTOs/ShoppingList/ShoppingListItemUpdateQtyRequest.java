package com.kupreu.api.DTOs.ShoppingList;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NonNull;

@Data
public class ShoppingListItemUpdateQtyRequest {
    @NonNull
    @Min(value = 1, message = "The quantity has to be more or equal than 1")
    private Integer quantity;
}
