package com.kupreu.api.controller;

import com.kupreu.api.DTOs.Attribute.AttributeRequest;
import com.kupreu.api.DTOs.Attribute.AttributeResponse;
import com.kupreu.api.service.AttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing product attribute endpoints under {@code /api/attributes}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attributes")
public class AttributeController {
    private final AttributeService service;

    /**
     * Returns all attributes.
     *
     * @return HTTP 200 with the list of attributes
     */
    @GetMapping
    public ResponseEntity<List<AttributeResponse>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Returns a single attribute by id.
     *
     * @param id the attribute identifier
     * @return HTTP 200 with the attribute
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttributeResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Creates a new attribute. Requires the {@code ADMIN} role.
     *
     * @param request the validated attribute data
     * @return HTTP 200 with the created attribute
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttributeResponse> create(@Valid @RequestBody AttributeRequest request){
        return ResponseEntity.ok(service.create(request));
    }

    /**
     * Updates an existing attribute. Requires the {@code ADMIN} role.
     *
     * @param id      the attribute identifier
     * @param request the validated attribute data
     * @return HTTP 200 with the updated attribute
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttributeResponse> update(@PathVariable UUID id, @Valid @RequestBody AttributeRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Deletes an attribute. Requires the {@code ADMIN} role.
     *
     * @param id the attribute identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
