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

/**
 * REST controller exposing store endpoints under {@code /api/stores}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;

    /**
     * Returns all stores.
     *
     * @return HTTP 200 with the list of stores
     */
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAll(){
        return ResponseEntity.ok(storeService.getAll());
    }

    /**
     * Returns a single store by id.
     *
     * @param id the store identifier
     * @return HTTP 200 with the store
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(storeService.getById(id));
    }

    /**
     * Creates a new store. Requires the {@code ADMIN} role.
     *
     * @param request the validated store data
     * @return HTTP 200 with the created store
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> create(@Valid @RequestBody StoreRequest request){
        return ResponseEntity.ok(storeService.create(request));
    }

    /**
     * Updates an existing store. Requires the {@code ADMIN} role.
     *
     * @param id      the store identifier
     * @param request the validated store data
     * @return HTTP 200 with the updated store
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponse> update(@PathVariable UUID id, @Valid @RequestBody StoreRequest request){
        return ResponseEntity.ok(storeService.update(id, request));
    }

    /**
     * Deletes a store. Requires the {@code ADMIN} role.
     *
     * @param id the store identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        storeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
