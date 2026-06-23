package com.kupreu.api.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.DateDIM;

/**
 * Spring Data JPA repository for {@link DateDIM} date-dimension entities.
 */
@Repository
public interface DateDIMRepository extends JpaRepository<DateDIM, UUID> {

    /**
     * Finds the date-dimension row for an exact date/time, so it can be reused
     * instead of inserting a duplicate.
     *
     * @param date the date/time to look up
     * @return the matching row, if present
     */
    Optional<DateDIM> findByDate(LocalDateTime date);
}
