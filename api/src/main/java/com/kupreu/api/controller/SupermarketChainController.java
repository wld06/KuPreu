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

/**
 * REST controller exposing supermarket-chain endpoints under {@code /api/chains}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chains")
public class SupermarketChainController {
    private final SupermarketChainService service;

    /**
     * Returns all supermarket chains with their store counts.
     *
     * @return HTTP 200 with the list of chains
     */
    @GetMapping
    public ResponseEntity<List<SupermarketChainResponse>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Returns a single chain, with its stores, by id.
     *
     * @param id the chain identifier
     * @return HTTP 200 with the chain
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupermarketChainWithStoresResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Creates a new supermarket chain. Requires the {@code ADMIN} role.
     *
     * @param request the validated chain data
     * @return HTTP 200 with the created chain
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupermarketChainWithStoresResponse> create(@Valid @RequestBody SupermarketChainRequest request){
        return ResponseEntity.ok(service.create(request));
    }

    /**
     * Updates an existing supermarket chain. Requires the {@code ADMIN} role.
     *
     * @param id      the chain identifier
     * @param request the validated chain data
     * @return HTTP 200 with the updated chain
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupermarketChainWithStoresResponse> update(@PathVariable UUID id, @Valid @RequestBody SupermarketChainRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Deletes a supermarket chain. Requires the {@code ADMIN} role.
     *
     * @param id the chain identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}