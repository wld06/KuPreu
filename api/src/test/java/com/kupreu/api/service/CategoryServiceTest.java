package com.kupreu.api.service;

import com.kupreu.api.audit.AuditService;

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

import com.kupreu.api.DTOs.Category.CategoryRequest;
import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Category.CategoryWithSubcategoriesResponse;
import com.kupreu.api.entity.Category;
import com.kupreu.api.repository.CategoryRepository;
import com.kupreu.api.repository.SubcategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private SubcategoryRepository subcategoryRepository;
    @Mock private AuditService auditService;
    @InjectMocks private CategoryService categoryService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private Category entity(String name) {
        return Category.builder().Id(ID).name(name).build();
    }

    private CategoryRequest request(String name) {
        CategoryRequest req = new CategoryRequest();
        req.setName(name);
        return req;
    }

    @Test
    void getAll_empty_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());
        assertThat(categoryService.getAll()).isEmpty();
    }

    @Test
    void getAll_mapsEntitiesWithSubcategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(entity("Lácteos"), entity("Bebidas")));
        when(subcategoryRepository.findByCategoryId(any(UUID.class))).thenReturn(List.of());

        List<CategoryWithSubcategoriesResponse> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Lácteos");
        assertThat(result.get(1).getName()).isEqualTo("Bebidas");
    }

    @Test
    void getById_found_returnsResponseWithSubcategories() {
        when(categoryRepository.findById(ID)).thenReturn(Optional.of(entity("Lácteos")));
        when(subcategoryRepository.findByCategoryId(ID)).thenReturn(List.of());

        CategoryWithSubcategoriesResponse res = categoryService.getById(ID);

        assertThat(res.getId()).isEqualTo(ID);
        assertThat(res.getName()).isEqualTo("Lácteos");
        assertThat(res.getSubcategories()).isEmpty();
    }

    @Test
    void getById_notFound_throws() {
        when(categoryRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found with id: " + ID);
    }

    @Test
    void create_newName_saves() {
        when(categoryRepository.existsByName("Lácteos")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(entity("Lácteos"));

        CategoryResponse res = categoryService.create(request("Lácteos"));

        assertThat(res.getName()).isEqualTo("Lácteos");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_duplicateName_throwsAndDoesNotSave() {
        when(categoryRepository.existsByName("Lácteos")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request("Lácteos")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_found_updatesName() {
        Category existing = entity("Lácteos");
        when(categoryRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse res = categoryService.update(ID, request("Lácteos y Huevos"));

        assertThat(res.getName()).isEqualTo("Lácteos y Huevos");
        assertThat(existing.getName()).isEqualTo("Lácteos y Huevos");
    }

    @Test
    void update_notFound_throws() {
        when(categoryRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(ID, request("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void delete_found_deletes() {
        when(categoryRepository.existsById(ID)).thenReturn(true);

        categoryService.delete(ID);

        verify(categoryRepository).deleteById(ID);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(categoryRepository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.delete(ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        verify(categoryRepository, never()).deleteById(any());
    }
}
