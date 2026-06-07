package com.kupreu.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    
}
