package com.kupreu.api.service;

import com.kupreu.api.exception.NotFoundException;
import com.kupreu.api.exception.BadRequestException;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryRequest;
import com.kupreu.api.DTOs.Subcategory.SubcategoryResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryWithCategory;
import com.kupreu.api.audit.AuditService;
import com.kupreu.api.entity.Category;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.repository.CategoryRepository;
import com.kupreu.api.repository.SubcategoryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SubcategoryService {
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    public SubcategoryWithCategory getById(UUID id){
        Subcategory sub =  subcategoryRepository.findById(id)
                            .orElseThrow(() -> new NotFoundException("Subcategory with id " + id + " does not exist"));
        return toResponseWithCategory(sub);
    }

    @Transactional
    public void delete(UUID id){
        Subcategory subcategory = subcategoryRepository.findById(id)
                                    .orElseThrow(() -> new NotFoundException("Subcategory with id " + id + " does not exist"));
        subcategoryRepository.delete(subcategory);
        auditService.record("SUBCATEGORY_DELETED", "Subcategory deleted", "id=" + id, true);
    }

    @Transactional
    public SubcategoryWithCategory update(UUID id, SubcategoryRequest request){
        Subcategory subcategory = subcategoryRepository.findById(id)
                                    .orElseThrow(() -> new NotFoundException("Subcategory not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new NotFoundException("Category not found"));

        subcategory.setName(request.getName());
        subcategory.setCategory(category);

        subcategoryRepository.save(subcategory);

        auditService.record("SUBCATEGORY_UPDATED", "Subcategory updated",
                "id=" + id + ", name=" + subcategory.getName(), true);

        return toResponseWithCategory(subcategory);
    }

    @Transactional
    public SubcategoryWithCategory create(SubcategoryRequest request){
        if (request.getCategoryId() == null){
            throw new BadRequestException("A category id is required");
        }
        if (request.getName() == null){
            throw new BadRequestException("A name is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category with id " + request.getCategoryId() + " does not exist"));

        Subcategory subcategory = subcategoryRepository.save(
            Subcategory.builder()
                .name(request.getName())
                .category(category)
                .build()
        );

        auditService.record("SUBCATEGORY_CREATED", "Subcategory created",
                "id=" + subcategory.getId() + ", name=" + subcategory.getName(), true);

        return toResponseWithCategory(subcategory);
    }

    private SubcategoryWithCategory toResponseWithCategory(Subcategory subcategory){
        Category category = categoryRepository.findById(subcategory.getCategory().getId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return SubcategoryWithCategory.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .category(CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .build();
    }

    private SubcategoryResponse toResponse(Subcategory subcategory){
        return SubcategoryResponse.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
        .build();
    }
}
