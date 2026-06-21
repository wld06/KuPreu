package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.ConflictException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.Category.CategoryRequest;
import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Category.CategoryWithSubcategoriesResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryResponse;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.entity.Category;
import com.kupreu.api.repository.CategoryRepository;
import com.kupreu.api.repository.SubcategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final AuditService auditService;

    public List<CategoryWithSubcategoriesResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponseWithSubcategories)
                .collect(Collectors.toList());
    }

    public CategoryWithSubcategoriesResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        return toResponseWithSubcategories(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())){
            throw new ConflictException("Category already exists with name: " + request.getName());
        }

        Category category = Category.builder()
                                    .name(request.getName())
                                    .build();

        category = categoryRepository.save(category);
        auditService.record("CATEGORY_CREATED", "Category created",
                "id=" + category.getId() + ", name=" + category.getName(), true);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request){
        Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Category not found"));
        category.setName(request.getName());
        category = categoryRepository.save(category);
        auditService.record("CATEGORY_UPDATED", "Category updated",
                "id=" + id + ", name=" + category.getName(), true);
        return toResponse(category);
    }

    @Transactional
    public void delete(UUID id){
        if (!categoryRepository.existsById(id)){
            throw new NotFoundException("Category not found");
        }

        categoryRepository.deleteById(id);
        auditService.record("CATEGORY_DELETED", "Category deleted", "id=" + id, true);
    }

    private CategoryWithSubcategoriesResponse toResponseWithSubcategories(Category category) {
        return CategoryWithSubcategoriesResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .subcategories(subcategoryRepository.findByCategoryId(category.getId())
                                        .stream()
                                        .map(subcategory -> SubcategoryResponse.builder()
                                                .id(subcategory.getId())
                                                .name(subcategory.getName())
                                                .build())
                                        .collect(Collectors.toList()))
                .build();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
