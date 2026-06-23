package com.kupreu.api.repository;

import com.kupreu.api.entity.ShoppingList;
import com.kupreu.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ShoppingList} entities.
 */
@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {

    /**
     * Finds all shopping lists owned by the given user.
     *
     * @param userId the owner's identifier
     * @return the user's shopping lists
     */
    List<ShoppingList> findByUser(UUID userId);

    /**
     * Checks whether the user already has a list with the given name,
     * enforcing the per-user unique-name rule.
     *
     * @param name the list name
     * @param user the owning user
     * @return {@code true} if such a list already exists
     */
    boolean existsByNameAndUser(String name, User user);
}
