package com.kupreu.api.DTOs.ShoppingList;

import lombok.Data;
import lombok.NonNull;

@Data
public class ShoppingListRequest {
    @NonNull
    private String name;
}
