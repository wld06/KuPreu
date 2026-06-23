package com.kupreu.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.Subcategory.SubcategoryRequest;
import com.kupreu.api.DTOs.Subcategory.SubcategoryWithCategory;
import com.kupreu.api.service.SubcategoryService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing subcategory endpoints under {@code /api/subcategories}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoryController {
    private final SubcategoryService subcategoryService;

    /**
     * Returns a single subcategory, with its parent category, by id.
     *
     * @param id the subcategory identifier
     * @return HTTP 200 with the subcategory
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryWithCategory> getById(@PathVariable UUID id){
        return ResponseEntity.ok(subcategoryService.getById(id));
    }

    /**
     * Creates a new subcategory. Requires the {@code ADMIN} role.
     *
     * @param request the validated subcategory data
     * @return HTTP 200 with the created subcategory
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoryWithCategory> create(@Valid @RequestBody SubcategoryRequest request){
        return ResponseEntity.ok(subcategoryService.create(request));
    }

    /**
     * Updates an existing subcategory. Requires the {@code ADMIN} role.
     *
     * @param id      the subcategory identifier
     * @param request the validated subcategory data
     * @return HTTP 200 with the updated subcategory
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubcategoryWithCategory> update(@PathVariable UUID id, @Valid @RequestBody SubcategoryRequest request){
        return ResponseEntity.ok(subcategoryService.update(id, request));
    }

    /**
     * Deletes a subcategory. Requires the {@code ADMIN} role.
     *
     * @param id the subcategory identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        subcategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
