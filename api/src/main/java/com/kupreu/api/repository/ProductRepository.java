package com.kupreu.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Product;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * Adds specification support for dynamic, filtered product queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Checks whether a product with the given name already exists.
     *
     * @param name the product name to check
     * @return {@code true} if such a product exists
     */
    boolean existsByName(String name);

    /**
     * Finds all products belonging to a brand identified by its name.
     *
     * @param brandName the brand name
     * @return the matching products
     */
    List<Product> findByBrandName(String brandName);

    /**
     * Finds a product by its EAN barcode.
     *
     * @param ean the EAN barcode
     * @return the matching product, if present
     */
    Optional<Product> findByEan(String ean);
}