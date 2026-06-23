package com.kupreu.api.controller;

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing brand endpoints under {@code /api/brands}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {
    private final BrandService brandService;

    /**
     * Returns all brands, optionally filtered by an exact name.
     *
     * @param brandName optional exact name filter
     * @return HTTP 200 with the matching brands
     */
    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAll(@RequestParam(required = false) String brandName){
        return ResponseEntity.ok(brandService.getAll(brandName));
    }

    /**
     * Returns a single brand by id.
     *
     * @param id the brand identifier
     * @return HTTP 200 with the brand
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(brandService.getById(id));
    }

    /**
     * Creates a new brand. Requires the {@code ADMIN} role.
     *
     * @param request the validated brand data
     * @return HTTP 200 with the created brand
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request){
        return ResponseEntity.ok(brandService.create(request));
    }

    /**
     * Updates an existing brand. Requires the {@code ADMIN} role.
     *
     * @param request the validated brand data
     * @param id      the brand identifier
     * @return HTTP 200 with the updated brand
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> update(@Valid @RequestBody BrandRequest request, @PathVariable UUID id){
        return ResponseEntity.ok(brandService.update(id, request));
    }

    /**
     * Deletes a brand. Requires the {@code ADMIN} role.
     *
     * @param id the brand identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
