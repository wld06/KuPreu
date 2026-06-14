package com.kupreu.api.controller;

import com.kupreu.api.DTOs.Store.StoreRequest;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAll(){
        return ResponseEntity.ok(storeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(storeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request){
        return ResponseEntity.ok(storeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> update(@PathVariable UUID id, @Valid @RequestBody StoreRequest request){
        return ResponseEntity.ok(storeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
