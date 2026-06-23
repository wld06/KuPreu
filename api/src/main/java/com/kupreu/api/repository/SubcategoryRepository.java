package com.kupreu.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Subcategory;

/**
 * Spring Data JPA repository for {@link Subcategory} entities.
 */
@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {

    /**
     * Finds all subcategories belonging to a given category.
     *
     * @param categoryId the parent category identifier
     * @return the matching subcategories
     */
    List<Subcategory> findByCategoryId(UUID categoryId);

    /**
     * Checks whether a subcategory with the given name already exists.
     *
     * @param name the subcategory name to check
     * @return {@code true} if such a subcategory exists
     */
    boolean existsByName(String name);
}
