package com.kupreu.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.UnitOfMeasure;

/**
 * Spring Data JPA repository for {@link UnitOfMeasure} entities.
 * Provides the standard CRUD operations keyed by UUID.
 */
@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
}
