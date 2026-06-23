package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.ConflictException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.Product.ProductRequest;
import com.kupreu.api.DTOs.Product.ProductResponse;
import com.kupreu.api.entity.Brand;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.entity.UnitOfMeasure;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.repository.BrandRepository;
import com.kupreu.api.repository.ProductRepository;
import com.kupreu.api.repository.SubcategoryRepository;
import com.kupreu.api.repository.UnitOfMeasureRepository;

import lombok.RequiredArgsConstructor;

/**
 * Application service holding the business logic for {@link Product} management:
 * CRUD operations plus filtered, paginated and brand/EAN-based lookups. Related
 * entities (subcategory, brand, unit of measure) are resolved on write, and every
 * mutating operation is recorded through the {@link AuditService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final AuditService auditService;

    /**
     * Returns every product across all brands.
     *
     * @return all products as response DTOs
     */
    public List<ProductResponse> getProductsFromAllBrands() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes the product with the given identifier.
     *
     * @param id the product identifier
     * @throws NotFoundException if no product has the given id
     */
    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)){
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
        auditService.record("PRODUCT_DELETED", "Product deleted", "id=" + id, true);
    }

    /**
     * Updates an existing product and its related entities.
     *
     * @param id      the product identifier
     * @param request the new product data, including subcategory, brand and unit references
     * @return the updated product as a response DTO
     * @throws NotFoundException if the product or any referenced entity does not exist
     * @throws ConflictException if the new name collides with another existing product
     */
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())){
            throw new ConflictException("Product with the same name already exists");
        }

        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
            .orElseThrow(() -> new NotFoundException("Subcategory not found"));
       Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new NotFoundException("Brand not found"));
       UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.getUnitOfMeasureId())
            .orElseThrow(() -> new NotFoundException("Unit of Measure not found"));
        
        product.setName(request.getName());
        product.setEan(request.getEan());
        product.setStockQty(request.getStock());
        product.setSubcategory(subcategory);
        product.setBrand(brand);
        product.setUnitOfMeasure(unitOfMeasure);

        product = productRepository.save(product);
        auditService.record("PRODUCT_UPDATED", "Product updated",
                "id=" + product.getId() + ", name=" + product.getName(), true);
        return toResponse(product);
    }

    /**
     * Creates a new product, resolving its subcategory, brand and unit of measure.
     *
     * @param request the product data
     * @return the created product as a response DTO
     * @throws ConflictException if a product with the same name already exists
     * @throws NotFoundException if any referenced entity does not exist
     */
    @Transactional
    public ProductResponse create(ProductRequest request){
       if (productRepository.existsByName(request.getName())){
            throw new ConflictException("Product with the same name already exists");
       }

       Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
            .orElseThrow(() -> new NotFoundException("Subcategory not found"));
       Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new NotFoundException("Brand not found"));
       UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.getUnitOfMeasureId())
            .orElseThrow(() -> new NotFoundException("Unit of Measure not found"));

       Product product = Product.builder()
            .name(request.getName())
            .ean(request.getEan())
            .stockQty(request.getStock())
            .subcategory(subcategory)
            .brand(brand)
            .unitOfMeasure(unitOfMeasure)
            .build();

        product = productRepository.save(product);
        auditService.record("PRODUCT_CREATED", "Product created",
                "id=" + product.getId() + ", name=" + product.getName(), true);
        return toResponse(product);
    }

    /**
     * Returns all products belonging to a brand identified by name.
     *
     * @param brandName the brand name
     * @return the matching products as response DTOs
     */
    public List<ProductResponse> getProductsFromBrand(String brandName) {
        return productRepository.findByBrandName(brandName)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Looks up a single product by its identifier.
     *
     * @param id the product identifier
     * @return the matching product as a response DTO
     * @throws NotFoundException if no product has the given id
     */
    public ProductResponse getProductById(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    /**
     * Looks up a single product by its EAN barcode.
     *
     * @param ean the EAN barcode
     * @return the matching product as a response DTO
     * @throws NotFoundException if no product has the given EAN
     */
    public ProductResponse getByEan(String ean){
        Product product = productRepository.findByEan(ean)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    /**
     * Returns a paginated list of products matching the given optional filters.
     * Any {@code null} or blank filter is ignored, so they can be combined freely.
     *
     * @param search        case-insensitive substring matched against the product name
     * @param categoryId    restrict to products under this category
     * @param subcategoryId restrict to products under this subcategory
     * @param brandId       restrict to products of this brand
     * @param pageable      pagination and sorting information
     * @return a page of matching products as response DTOs
     */
    public Page<ProductResponse> getProducts(String search, UUID categoryId, UUID subcategoryId, UUID brandId, Pageable pageable) {
        Specification<Product> spec = Specification.where(Specification.unrestricted());

        if (search != null && !search.isBlank()){
            spec = spec.and((root, q, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }

        if (subcategoryId != null){
            spec = spec.and((root, q, cb) ->
                cb.equal(root.get("subcategory").get("id"), subcategoryId));
        }

        if (categoryId != null){
            spec = spec.and((root, q, cb) -> 
                cb.equal(root.get("subcategory").get("category").get("id"), categoryId));
        }

        if (brandId != null){
            spec = spec.and((root, q, cb) ->
                cb.equal(root.get("brand").get("id"), brandId));
        }

        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    /** Maps a {@link Product} entity to its response DTO with related entity names. */
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .ean(product.getEan())
                .name(product.getName())
                .stockQty(product.getStockQty())
                .subcategoryName(product.getSubcategory().getName())
                .brandName(product.getBrand().getName())
                .unitOfMeasureName(product.getUnitOfMeasure().getName())
                .build();
    }
}
