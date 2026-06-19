package com.kupreu.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.kupreu.api.DTOs.Product.ProductRequest;
import com.kupreu.api.DTOs.Product.ProductResponse;
import com.kupreu.api.entity.Brand;
import com.kupreu.api.entity.Category;
import com.kupreu.api.entity.Product;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.entity.UnitOfMeasure;
import com.kupreu.api.repository.BrandRepository;
import com.kupreu.api.repository.ProductRepository;
import com.kupreu.api.repository.SubcategoryRepository;
import com.kupreu.api.repository.UnitOfMeasureRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private SubcategoryRepository subcategoryRepository;
    @Mock private UnitOfMeasureRepository unitOfMeasureRepository;
    @InjectMocks private ProductService productService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SUB_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID BRAND_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID UOM_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private Subcategory subcategory() {
        return Subcategory.builder().id(SUB_ID).name("Yogures")
                .category(Category.builder().Id(UUID.randomUUID()).name("Lácteos").build()).build();
    }

    private Brand brand() {
        return Brand.builder().Id(BRAND_ID).name("Hacendado").build();
    }

    private UnitOfMeasure unit() {
        return UnitOfMeasure.builder().Id(UOM_ID).name("Litro").symbol("L").build();
    }

    private Product entity(String name) {
        return Product.builder()
                .id(ID).ean("1234567890123").name(name).stockQty(10)
                .subcategory(subcategory()).brand(brand()).unitOfMeasure(unit())
                .build();
    }

    private ProductRequest request(String name) {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setEan("1234567890123");
        req.setStock(10);
        req.setSubcategoryId(SUB_ID);
        req.setBrandId(BRAND_ID);
        req.setUnitOfMeasureId(UOM_ID);
        return req;
    }

    // ── read ────────────────────────────────────────────────────────────────────

    @Test
    void getProductsFromAllBrands_mapsResponses() {
        when(productRepository.findAll()).thenReturn(List.of(entity("Leche"), entity("Yogur")));

        List<ProductResponse> result = productService.getProductsFromAllBrands();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Leche");
        assertThat(result.get(0).getBrandName()).isEqualTo("Hacendado");
        assertThat(result.get(0).getSubcategoryName()).isEqualTo("Yogures");
        assertThat(result.get(0).getUnitOfMeasureName()).isEqualTo("Litro");
    }

    @Test
    void getProductsFromBrand_delegatesToRepository() {
        when(productRepository.findByBrandName("Hacendado")).thenReturn(List.of(entity("Leche")));

        assertThat(productService.getProductsFromBrand("Hacendado")).hasSize(1);
    }

    @Test
    void getProductById_found_returnsResponse() {
        when(productRepository.findById(ID)).thenReturn(Optional.of(entity("Leche")));

        ProductResponse res = productService.getProductById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getName()).isEqualTo("Leche");
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");
    }

    @Test
    void getByEan_found_returnsResponse() {
        when(productRepository.findByEan("1234567890123")).thenReturn(Optional.of(entity("Leche")));

        assertThat(productService.getByEan("1234567890123").getName()).isEqualTo("Leche");
    }

    @Test
    void getByEan_notFound_throws() {
        when(productRepository.findByEan("000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getByEan("000"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProducts_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(entity("Leche")), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> result = productService.getProducts("lec", null, SUB_ID, BRAND_ID, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Leche");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_valid_saves() {
        when(productRepository.existsByName("Leche")).thenReturn(false);
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(subcategory()));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(unitOfMeasureRepository.findById(UOM_ID)).thenReturn(Optional.of(unit()));
        when(productRepository.save(any(Product.class))).thenReturn(entity("Leche"));

        ProductResponse res = productService.create(request("Leche"));

        assertThat(res.getName()).isEqualTo("Leche");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_duplicateName_throwsAndDoesNotSave() {
        when(productRepository.existsByName("Leche")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request("Leche")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product with the same name already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_subcategoryNotFound_throws() {
        when(productRepository.existsByName("Leche")).thenReturn(false);
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request("Leche")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Subcategory not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_brandNotFound_throws() {
        when(productRepository.existsByName("Leche")).thenReturn(false);
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(subcategory()));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request("Leche")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Brand not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_unitOfMeasureNotFound_throws() {
        when(productRepository.existsByName("Leche")).thenReturn(false);
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(subcategory()));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(unitOfMeasureRepository.findById(UOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request("Leche")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unit of Measure not found");

        verify(productRepository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    void update_found_sameName_skipsDuplicateCheck() {
        Product existing = entity("Leche");
        when(productRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(subcategory()));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(unitOfMeasureRepository.findById(UOM_ID)).thenReturn(Optional.of(unit()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse res = productService.update(ID, request("Leche"));

        assertThat(res.getName()).isEqualTo("Leche");
        verify(productRepository, never()).existsByName(any());
    }

    @Test
    void update_found_newName_checksDuplicate() {
        Product existing = entity("Leche");
        when(productRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(productRepository.existsByName("Leche Entera")).thenReturn(false);
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(subcategory()));
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(unitOfMeasureRepository.findById(UOM_ID)).thenReturn(Optional.of(unit()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse res = productService.update(ID, request("Leche Entera"));

        assertThat(res.getName()).isEqualTo("Leche Entera");
    }

    @Test
    void update_notFound_throws() {
        when(productRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(ID, request("Leche")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_newNameAlreadyTaken_throwsAndDoesNotSave() {
        when(productRepository.findById(ID)).thenReturn(Optional.of(entity("Leche")));
        when(productRepository.existsByName("Leche Entera")).thenReturn(true);

        assertThatThrownBy(() -> productService.update(ID, request("Leche Entera")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product with the same name already exists");

        verify(productRepository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletes() {
        when(productRepository.existsById(ID)).thenReturn(true);

        productService.delete(ID);

        verify(productRepository).deleteById(ID);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(productRepository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Product not found");

        verify(productRepository, never()).deleteById(any());
    }
}
