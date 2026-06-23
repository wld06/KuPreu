package com.kupreu.api.DTOs.ShoppingList;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response payload representing a shopping list along with its items.
 */
@Data
@Builder
public class ShoppingListResponse {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private List<ShoppingListItemResponse> shoppingListItems;
}