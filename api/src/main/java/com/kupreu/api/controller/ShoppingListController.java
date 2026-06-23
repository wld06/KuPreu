package com.kupreu.api.controller;

import com.kupreu.api.DTOs.ShoppingList.ShoppingListItemRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListItemUpdateQtyRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListRequest;
import com.kupreu.api.DTOs.ShoppingList.ShoppingListResponse;
import com.kupreu.api.service.ShoppingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing shopping-list endpoints under {@code /api/shopping-lists}.
 * Every endpoint operates on the authenticated user's own lists, resolved from the
 * security principal.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService service;

    /**
     * Returns a list with each item priced at the cheapest currently-valid offer.
     *
     * @param id          the shopping list identifier
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the list using cheapest prices
     */
    @GetMapping("/{id}/cheapest")
    public ResponseEntity<ShoppingListResponse> getCheapest(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.getCheapestList(id, userDetails.getUsername()));
    }

    /**
     * Returns all shopping lists owned by the authenticated user.
     *
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the user's lists
     */
    @GetMapping
    public ResponseEntity<List<ShoppingListResponse>> getAll(@AuthenticationPrincipal  UserDetails userDetails){
        return ResponseEntity.ok(service.getAll(userDetails.getUsername()));
    }

    /**
     * Returns a single shopping list owned by the authenticated user.
     *
     * @param id          the shopping list identifier
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the list
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.getById(id, userDetails.getUsername()));
    }

    /**
     * Creates a new shopping list for the authenticated user.
     *
     * @param userDetails the authenticated principal
     * @param request     the validated list data
     * @return HTTP 200 with the created list
     */
    @PostMapping
    public ResponseEntity<ShoppingListResponse> create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ShoppingListRequest request){
        return ResponseEntity.ok(service.create(request, userDetails.getUsername()));
    }

    /**
     * Renames a shopping list owned by the authenticated user.
     *
     * @param id          the shopping list identifier
     * @param userDetails the authenticated principal
     * @param request     the validated list data
     * @return HTTP 200 with the updated list
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> update(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ShoppingListRequest request){
        return ResponseEntity.ok(service.update(id, request, userDetails.getUsername()));
    }

    /**
     * Deletes a shopping list owned by the authenticated user.
     *
     * @param id          the shopping list identifier
     * @param userDetails the authenticated principal
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        service.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds an item to a shopping list (or increases its quantity if already present).
     *
     * @param id          the shopping list identifier
     * @param request     the validated item data
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the updated list
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingListResponse> addItem(@PathVariable UUID id, @Valid @RequestBody ShoppingListItemRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.addItem(id, request, userDetails.getUsername()));
    }

    /**
     * Updates the quantity of an item within a shopping list.
     *
     * @param id          the shopping list identifier
     * @param itemId      the item identifier
     * @param request     the validated new quantity
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the updated list
     */
    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ShoppingListResponse> updateItem(@PathVariable UUID id, @PathVariable UUID itemId,
                                                           @Valid @RequestBody ShoppingListItemUpdateQtyRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.updateQuantityItem(id, itemId, request, userDetails.getUsername()));
    }

    /**
     * Removes an item from a shopping list.
     *
     * @param id          the shopping list identifier
     * @param itemId      the item identifier
     * @param userDetails the authenticated principal
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id, @PathVariable UUID itemId,
                                           @AuthenticationPrincipal UserDetails userDetails){
        service.deleteItem(id, itemId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
