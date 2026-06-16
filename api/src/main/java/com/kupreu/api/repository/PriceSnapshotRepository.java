package com.kupreu.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.PriceSnapshot;
import com.kupreu.api.entity.PriceSnapshotId;

@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, PriceSnapshotId> {
    List<PriceSnapshot> findByProductId(UUID productId);
    List<PriceSnapshot> findByProductIdAndStoreId(UUID productId, UUID storeId);
    PriceSnapshot findByUuid(UUID uuid);
    Optional<PriceSnapshot> findFirstByProductIdAndDateEndIsNullOrderByPriceAsc(UUID productId);
}
