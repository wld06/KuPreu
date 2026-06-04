package com.kupreu.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Subcategory;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {
}
