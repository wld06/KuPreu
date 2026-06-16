package com.kupreu.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {
    List<Brand> findByName(String name);
}