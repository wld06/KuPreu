package com.kupreu.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.Category.CategoryRequest;
import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Category.CategoryWithSubcategoriesResponse;
import com.kupreu.api.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing category endpoints under {@code /api/categories}.
 * Reads are public; create, update and delete require the {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Returns all categories, each with its subcategories.
     *
     * @return HTTP 200 with the categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryWithSubcategoriesResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /**
     * Returns a single category, with its subcategories, by id.
     *
     * @param id the category identifier
     * @return HTTP 200 with the category
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryWithSubcategoriesResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Creates a new category. Requires the {@code ADMIN} role.
     *
     * @param request the validated category data
     * @return HTTP 201 with the created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /**
     * Updates an existing category. Requires the {@code ADMIN} role.
     *
     * @param id      the category identifier
     * @param request the validated category data
     * @return HTTP 200 with the updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    /**
     * Deletes a category. Requires the {@code ADMIN} role.
     *
     * @param id the category identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
