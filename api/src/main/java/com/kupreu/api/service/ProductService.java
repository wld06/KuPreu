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

import com.kupreu.api.DTOs.Product.ProductRequest;
import com.kupreu.api.DTOs.Product.ProductResponse;
import com.kupreu.api.entity.Brand;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.entity.UnitOfMeasure;
import com.kupreu.api.repository.BrandRepository;
import com.kupreu.api.repository.ProductRepository;
import com.kupreu.api.repository.SubcategoryRepository;
import com.kupreu.api.repository.UnitOfMeasureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public List<ProductResponse> getProductsFromAllBrands() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        if (!productRepository.existsById(id)){
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getName().equals(request.name) && productRepository.existsByName(request.name)){
            throw new ConflictException("Product with the same name already exists");
        }

        Subcategory subcategory = subcategoryRepository.findById(request.subcategoryId)
            .orElseThrow(() -> new NotFoundException("Subcategory not found"));
       Brand brand = brandRepository.findById(request.brandId)
            .orElseThrow(() -> new NotFoundException("Brand not found"));
       UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.unitOfMeasureId)
            .orElseThrow(() -> new NotFoundException("Unit of Measure not found"));
        
        product.setName(request.name);
        product.setEan(request.ean);
        product.setStockQty(request.stock);
        product.setSubcategory(subcategory);
        product.setBrand(brand);
        product.setUnitOfMeasure(unitOfMeasure);

        return toResponse(productRepository.save(product));
    }

    public ProductResponse create(ProductRequest request){
       if (productRepository.existsByName(request.name)){
            throw new ConflictException("Product with the same name already exists");
       }

       Subcategory subcategory = subcategoryRepository.findById(request.subcategoryId)
            .orElseThrow(() -> new NotFoundException("Subcategory not found"));
       Brand brand = brandRepository.findById(request.brandId)
            .orElseThrow(() -> new NotFoundException("Brand not found"));
       UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.unitOfMeasureId)
            .orElseThrow(() -> new NotFoundException("Unit of Measure not found"));

       Product product = Product.builder()
            .name(request.name)
            .ean(request.ean)
            .stockQty(request.stock)
            .subcategory(subcategory)
            .brand(brand)
            .unitOfMeasure(unitOfMeasure)
            .build();

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> getProductsFromBrand(String brandName) {
        return productRepository.findByBrandName(brandName)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    public ProductResponse getByEan(String ean){
        Product product = productRepository.findByEan(ean)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    public Page<ProductResponse> getProducts(String search, UUID categoryId, UUID subcategoryId, UUID brandId, Pageable pageable) {
        Specification<Product> spec = Specification.where((Specification<Product>) null);

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
