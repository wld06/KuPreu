package com.kupreu.api.repository;


import com.kupreu.api.entity.SupermarketChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SupermarketChain} entities.
 * Provides the standard CRUD operations keyed by UUID.
 */
@Repository
public interface SupermarketChainRepository extends JpaRepository<SupermarketChain, UUID> {
}
