package com.kupreu.api.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.DateDIM;

@Repository
public interface DateDIMRepository extends JpaRepository<DateDIM, UUID> {
    Optional<DateDIM> findByDate(LocalDateTime date);
}
