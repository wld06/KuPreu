package com.kupreu.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Brand;

/**
 * Spring Data JPA repository for {@link Brand} entities.
 * Adds specification support for dynamic queries on top of the standard CRUD operations.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {

    /**
     * Finds all brands whose name matches the given value exactly.
     *
     * @param name the brand name to search for
     * @return the matching brands, or an empty list if none match
     */
    List<Brand> findByName(String name);
}