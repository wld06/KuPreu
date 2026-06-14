package com.kupreu.api.controller;

import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainRequest;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainWithStoresResponse;
import com.kupreu.api.service.SupermarketChainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chains")
public class SupermarketChainController {
    private final SupermarketChainService service;

    @GetMapping
    public ResponseEntity<List<SupermarketChainResponse>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupermarketChainWithStoresResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupermarketChainWithStoresResponse> create(@Valid @RequestBody SupermarketChainRequest request){
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupermarketChainWithStoresResponse> update(@PathVariable UUID id, @Valid @RequestBody SupermarketChainRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}