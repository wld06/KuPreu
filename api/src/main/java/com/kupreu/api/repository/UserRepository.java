package com.kupreu.api.repository;

import java.util.Optional;
import java.util.UUID;

import com.kupreu.api.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.User;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

    /**
     * Finds a user by e-mail address, typically used during authentication.
     *
     * @param email the e-mail to look up
     * @return the matching user, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an account with the given e-mail already exists.
     *
     * @param email the e-mail to check
     * @return {@code true} if the e-mail is already registered
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether an account with the given username already exists.
     *
     * @param username the username to check
     * @return {@code true} if the username is already taken
     */
    boolean existsByUsername(String username);
}
