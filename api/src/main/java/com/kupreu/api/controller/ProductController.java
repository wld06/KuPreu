package com.kupreu.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.Product.ProductRequest;
import com.kupreu.api.DTOs.Product.ProductResponse;
import com.kupreu.api.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing product endpoints under {@code /api/products}.
 * Reads (including filtered, paginated search) are public; create, update and
 * delete require the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    /**
     * Returns a paginated, optionally filtered list of products.
     *
     * @param search        optional case-insensitive name substring
     * @param categoryId    optional category filter
     * @param subcategoryId optional subcategory filter
     * @param brandId       optional brand filter
     * @param pageable      pagination and sorting (defaults to size 20)
     * @return HTTP 200 with a page of products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) UUID subcategoryId,
        @RequestParam(required = false) UUID brandId,
        @PageableDefault(size = 20) Pageable pageable
    ){
        return ResponseEntity.ok(productService.getProducts(search, categoryId, subcategoryId, brandId, pageable));
    }

    /**
     * Returns a single product by id.
     *
     * @param id the product identifier
     * @return HTTP 200 with the product
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Returns a single product by its EAN barcode.
     *
     * @param ean the EAN barcode
     * @return HTTP 200 with the product
     */
    @GetMapping("/ean/{ean}")
    public ResponseEntity<ProductResponse> getByEan(@PathVariable String ean) {
        return ResponseEntity.ok(productService.getByEan(ean));
    }

    /**
     * Creates a new product. Requires the {@code ADMIN} role.
     *
     * @param request the validated product data
     * @return HTTP 200 with the created product
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.create(request));
    }

    /**
     * Updates an existing product. Requires the {@code ADMIN} role.
     *
     * @param id      the product identifier
     * @param request the validated product data
     * @return HTTP 200 with the updated product
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * Deletes a product. Requires the {@code ADMIN} role.
     *
     * @param id the product identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}