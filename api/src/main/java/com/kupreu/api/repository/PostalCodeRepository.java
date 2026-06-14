package com.kupreu.api.repository;

import com.kupreu.api.entity.PostalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostalCodeRepository extends JpaRepository<PostalCode, UUID> {
    Optional<PostalCode> findByCode(String code);
}
