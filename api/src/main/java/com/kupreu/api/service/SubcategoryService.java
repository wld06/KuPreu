package com.kupreu.api.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryRequest;
import com.kupreu.api.DTOs.Subcategory.SubcategoryResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryWithCategory;
import com.kupreu.api.entity.Category;
import com.kupreu.api.entity.Subcategory;
import com.kupreu.api.repository.CategoryRepository;
import com.kupreu.api.repository.SubcategoryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubcategoryService {
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;

    public SubcategoryWithCategory getById(UUID id){
        Subcategory sub =  subcategoryRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Subcategory with id " + id + " does not exist"));
        return toResponseWithCategory(sub);
    }

    public void delete(UUID id){
        Subcategory subcategory = subcategoryRepository.findById(id)
                                    .orElseThrow(() -> new RuntimeException("Subcategory with id " + id + " does not exist"));
        subcategoryRepository.delete(subcategory);
    }

    public SubcategoryWithCategory update(UUID id, SubcategoryRequest request){
        Subcategory subcategory = subcategoryRepository.findById(id)
                                    .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));

        subcategory.setName(request.getName());
        subcategory.setCategory(category);

        subcategoryRepository.save(subcategory);

        return toResponseWithCategory(subcategory);
    }

    public SubcategoryWithCategory create(SubcategoryRequest request){
        if (request.getCategoryId() == null){
            throw new RuntimeException("A category id is required");
        }
        if (request.getName() == null){
            throw new RuntimeException("A name is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category with id " + request.getCategoryId() + " does not exist"));

        Subcategory subcategory = subcategoryRepository.save(
            Subcategory.builder()
                .name(request.getName())
                .category(category)
                .build()
        );

        return toResponseWithCategory(subcategory);
    }

    private SubcategoryWithCategory toResponseWithCategory(Subcategory subcategory){
        Category category = categoryRepository.findById(subcategory.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
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
