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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService service;

    @GetMapping("/{id}/cheapest")
    public ResponseEntity<ShoppingListResponse> getCheapest(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.getCheapestList(id, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ShoppingListResponse>> getAll(@AuthenticationPrincipal  UserDetails userDetails){
        return ResponseEntity.ok(service.getAll(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> getById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.getById(id, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<ShoppingListResponse> create(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ShoppingListRequest request){
        return ResponseEntity.ok(service.create(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> update(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, @RequestBody ShoppingListRequest request){
        return ResponseEntity.ok(service.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        service.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingListResponse> addItem(@PathVariable UUID id, @Valid @RequestBody ShoppingListItemRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.addItem(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ShoppingListResponse> updateItem(@PathVariable UUID id, @PathVariable UUID itemId,
                                                           @Valid @RequestBody ShoppingListItemUpdateQtyRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(service.updateQuantityItem(id, itemId, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id, @PathVariable UUID itemId,
                                           @AuthenticationPrincipal UserDetails userDetails){
        service.deleteItem(id, itemId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
