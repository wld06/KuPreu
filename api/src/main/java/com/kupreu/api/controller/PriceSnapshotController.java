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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing price-snapshot endpoints under {@code /api}.
 * Provides a product's price history (optionally per store), its cheapest active
 * price, and creation of new snapshots.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PriceSnapshotController {

    private final PriceSnapshotService priceSnapshotService;

    /**
     * Returns the price history of a product, optionally narrowed to a single store.
     *
     * @param id      the product identifier
     * @param storeId optional store filter
     * @return HTTP 200 with the matching price snapshots, most recent first
     */
    @GetMapping("/products/{id}/prices")
    public ResponseEntity<List<PriceSnapshotResponse>> getPriceSnapshotsByProductId(@PathVariable UUID id, @RequestParam(required = false) UUID storeId){
        if (storeId != null) {
            return ResponseEntity.ok(priceSnapshotService.getPriceSnapshotsByProductIdAndStoreId(id, storeId));
        }

        return ResponseEntity.ok(priceSnapshotService.getPriceSnapshotByProductId(id));
    }

    /**
     * Returns the cheapest currently-valid price for a product.
     *
     * @param id the product identifier
     * @return HTTP 200 with the cheapest active price snapshot
     */
    @GetMapping("/products/{id}/prices/cheapest")
    public ResponseEntity<PriceSnapshotResponse> getCheapestPriceSnapshotByProductId(@PathVariable UUID id){
        return ResponseEntity.ok(priceSnapshotService.getCheapest(id));
    }

    /**
     * Creates a new price snapshot.
     *
     * @param request the validated snapshot data
     * @return HTTP 200 with the created price snapshot
     */
    @PostMapping("/prices")
    public ResponseEntity<PriceSnapshotResponse> createPriceSnapshot(@Valid @RequestBody PriceSnapshotRequest request){
        return ResponseEntity.ok(priceSnapshotService.create(request));
    }
}
