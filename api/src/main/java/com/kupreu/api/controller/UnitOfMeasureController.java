package com.kupreu.api.controller;

import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureRequest;
import com.kupreu.api.DTOs.UnitOfMeasure.UnitOfMeasureResponse;
import com.kupreu.api.service.UnitOfMeasureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing unit-of-measure endpoints under {@code /api/unitofmeasure}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/unitofmeasure")
public class UnitOfMeasureController {
    private final UnitOfMeasureService service;

    /**
     * Returns all units of measure.
     *
     * @return HTTP 200 with the list of units
     */
    @GetMapping
    public ResponseEntity<List<UnitOfMeasureResponse>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Returns a single unit of measure by id.
     *
     * @param id the unit identifier
     * @return HTTP 200 with the unit
     */
    @GetMapping("/{id}")
    public ResponseEntity<UnitOfMeasureResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * Creates a new unit of measure. Requires the {@code ADMIN} role.
     *
     * @param request the validated unit data
     * @return HTTP 200 with the created unit
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitOfMeasureResponse> create(@Valid @RequestBody UnitOfMeasureRequest request){
        return ResponseEntity.ok(service.create(request));
    }

    /**
     * Updates an existing unit of measure. Requires the {@code ADMIN} role.
     *
     * @param id      the unit identifier
     * @param request the validated unit data
     * @return HTTP 200 with the updated unit
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitOfMeasureResponse> update(@PathVariable UUID id, @Valid @RequestBody UnitOfMeasureRequest request){
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Deletes a unit of measure. Requires the {@code ADMIN} role.
     *
     * @param id the unit identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
