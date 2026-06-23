package com.kupreu.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Category;

/**
 * Spring Data JPA repository for {@link Category} entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Checks whether a category with the given name already exists.
     *
     * @param name the category name to check
     * @return {@code true} if a category with that name exists
     */
    boolean existsByName(String name);
}
