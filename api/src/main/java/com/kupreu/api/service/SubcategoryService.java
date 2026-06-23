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

/**
 * Application service holding the business logic for {@link Subcategory} management,
 * including resolving each subcategory's parent {@link Category}. Every mutating
 * operation is recorded through the {@link AuditService}.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class SubcategoryService {
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    /**
     * Looks up a single subcategory, with its parent category, by identifier.
     *
     * @param id the subcategory identifier
     * @return the matching subcategory including its category
     * @throws NotFoundException if no subcategory has the given id
     */
    public SubcategoryWithCategory getById(UUID id){
        Subcategory sub =  subcategoryRepository.findById(id)
                            .orElseThrow(() -> new NotFoundException("Subcategory with id " + id + " does not exist"));
        return toResponseWithCategory(sub);
    }

    /**
     * Deletes the subcategory with the given identifier.
     *
     * @param id the subcategory identifier
     * @throws NotFoundException if no subcategory has the given id
     */
    @Transactional
    public void delete(UUID id){
        Subcategory subcategory = subcategoryRepository.findById(id)
                                    .orElseThrow(() -> new NotFoundException("Subcategory with id " + id + " does not exist"));
        subcategoryRepository.delete(subcategory);
        auditService.record("SUBCATEGORY_DELETED", "Subcategory deleted", "id=" + id, true);
    }

    /**
     * Updates a subcategory's name and reassigns it to the given category.
     *
     * @param id      the subcategory identifier
     * @param request the new subcategory data, including the target category id
     * @return the updated subcategory including its category
     * @throws NotFoundException if the subcategory or target category does not exist
     */
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

    /**
     * Creates a new subcategory under an existing category.
     *
     * @param request the subcategory data; both name and category id are required
     * @return the created subcategory including its category
     * @throws BadRequestException if the name or category id is missing
     * @throws NotFoundException   if the referenced category does not exist
     */
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

    /** Maps a {@link Subcategory} entity to a response DTO including its parent category. */
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

    /** Maps a {@link Subcategory} entity to its plain response DTO. */
    private SubcategoryResponse toResponse(Subcategory subcategory){
        return SubcategoryResponse.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
        .build();
    }
}
