package com.kupreu.api.repository;

import com.kupreu.api.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ShoppingListItem} entities.
 * Provides the standard CRUD operations keyed by UUID.
 */
@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, UUID> {
}