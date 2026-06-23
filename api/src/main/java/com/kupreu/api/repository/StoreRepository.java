package com.kupreu.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Store;

/**
 * Spring Data JPA repository for {@link Store} entities.
 * Provides the standard CRUD operations keyed by UUID.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

}
