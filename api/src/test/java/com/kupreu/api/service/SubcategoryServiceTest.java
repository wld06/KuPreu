package com.kupreu.api.service;

import com.kupreu.api.audit.AuditService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kupreu.api.DTOs.Subcategory.SubcategoryRequest;
import com.kupreu.api.DTOs.Subcategory.SubcategoryWithCategory;
import com.kupreu.api.entity.Category;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.repository.CategoryRepository;
import com.kupreu.api.repository.SubcategoryRepository;

@ExtendWith(MockitoExtension.class)
class SubcategoryServiceTest {

    @Mock private SubcategoryRepository subcategoryRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AuditService auditService;
    @InjectMocks private SubcategoryService subcategoryService;

    private static final UUID SUB_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CAT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private Category category(String name) {
        return Category.builder().Id(CAT_ID).name(name).build();
    }

    private Subcategory entity(String name) {
        return Subcategory.builder().id(SUB_ID).name(name).category(category("Lácteos")).build();
    }

    private SubcategoryRequest request(String name, UUID categoryId) {
        SubcategoryRequest req = new SubcategoryRequest();
        req.setName(name);
        req.setCategoryId(categoryId);
        return req;
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsWithCategory() {
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(entity("Yogures")));
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.of(category("Lácteos")));

        SubcategoryWithCategory res = subcategoryService.getById(SUB_ID);

        assertThat(res.getId()).isEqualTo(SUB_ID);
        assertThat(res.getName()).isEqualTo("Yogures");
        assertThat(res.getCategory().getName()).isEqualTo("Lácteos");
    }

    @Test
    void getById_notFound_throws() {
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.getById(SUB_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not exist");
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    void create_valid_saves() {
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.of(category("Lácteos")));
        when(subcategoryRepository.save(any(Subcategory.class))).thenReturn(entity("Yogures"));

        SubcategoryWithCategory res = subcategoryService.create(request("Yogures", CAT_ID));

        assertThat(res.getName()).isEqualTo("Yogures");
        assertThat(res.getCategory().getName()).isEqualTo("Lácteos");
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    @Test
    void create_nullCategoryId_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> subcategoryService.create(request("Yogures", null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A category id is required");

        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void create_nullName_throwsAndDoesNotSave() {
        assertThatThrownBy(() -> subcategoryService.create(request(null, CAT_ID)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("A name is required");

        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void create_categoryNotFound_throwsAndDoesNotSave() {
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.create(request("Yogures", CAT_ID)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not exist");

        verify(subcategoryRepository, never()).save(any());
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    void update_found_updatesNameAndCategory() {
        Subcategory existing = entity("Yogures");
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.of(category("Lácteos")));

        SubcategoryWithCategory res = subcategoryService.update(SUB_ID, request("Postres", CAT_ID));

        assertThat(res.getName()).isEqualTo("Postres");
        assertThat(existing.getName()).isEqualTo("Postres");
        verify(subcategoryRepository).save(existing);
    }

    @Test
    void update_subcategoryNotFound_throws() {
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.update(SUB_ID, request("Postres", CAT_ID)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Subcategory not found");

        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void update_categoryNotFound_throws() {
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(entity("Yogures")));
        when(categoryRepository.findById(CAT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.update(SUB_ID, request("Postres", CAT_ID)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");

        verify(subcategoryRepository, never()).save(any());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletes() {
        Subcategory existing = entity("Yogures");
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.of(existing));

        subcategoryService.delete(SUB_ID);

        verify(subcategoryRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(subcategoryRepository.findById(SUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.delete(SUB_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not exist");

        verify(subcategoryRepository, never()).delete(any());
    }
}
