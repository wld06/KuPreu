package com.kupreu.api.repository;

import com.kupreu.api.entity.PostalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PostalCode} entities.
 */
@Repository
public interface PostalCodeRepository extends JpaRepository<PostalCode, UUID> {

    /**
     * Finds a postal code by its code value.
     *
     * @param code the postal code value to look up
     * @return the matching postal code, if present
     */
    Optional<PostalCode> findByCode(String code);
}
