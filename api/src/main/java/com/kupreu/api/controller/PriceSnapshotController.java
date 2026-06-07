package com.kupreu.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotRequest;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.service.PriceSnapshotService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PriceSnapshotController {
    
    private final PriceSnapshotService priceSnapshotService;

    @GetMapping("/products/{id}/prices")
    public ResponseEntity<List<PriceSnapshotResponse>> getPriceSnapshotsByProductId(@PathVariable UUID id, @RequestParam(required = false) UUID storeId){
        if (storeId != null) {
            return ResponseEntity.ok(priceSnapshotService.getPriceSnapshotsByProductIdAndStoreId(id, storeId));
        }
        
        return ResponseEntity.ok(priceSnapshotService.getPriceSnapshotByProductId(id));
    }

    @GetMapping("/products/{id}/prices/cheapest")
    public ResponseEntity<PriceSnapshotResponse> getCheapestPriceSnapshotByProductId(@PathVariable UUID id){
        return ResponseEntity.ok(priceSnapshotService.getCheapest(id));
    }

    @PostMapping("/prices")
    public ResponseEntity<PriceSnapshotResponse> createPriceSnapshot(@RequestBody PriceSnapshotRequest request){
        return ResponseEntity.ok(priceSnapshotService.create(request));
    }
}
