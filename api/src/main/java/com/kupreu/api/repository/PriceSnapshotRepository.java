package com.kupreu.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kupreu.api.entity.PriceSnapshot;
import com.kupreu.api.entity.PriceSnapshotId;

/**
 * Spring Data JPA repository for {@link PriceSnapshot} entities, keyed by the
 * composite {@link PriceSnapshotId}.
 */
@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, PriceSnapshotId> {

    /**
     * Finds every price snapshot recorded for a given product.
     *
     * @param productId the product identifier
     * @return the matching snapshots
     */
    List<PriceSnapshot> findByProductId(UUID productId);

    /**
     * Finds every price snapshot recorded for a given product at a given store.
     *
     * @param productId the product identifier
     * @param storeId   the store identifier
     * @return the matching snapshots
     */
    List<PriceSnapshot> findByProductIdAndStoreId(UUID productId, UUID storeId);

    /**
     * Finds a snapshot by its surrogate {@code uuid}.
     *
     * @param uuid the surrogate identifier
     * @return the matching snapshot, or {@code null} if none exists
     */
    PriceSnapshot findByUuid(UUID uuid);

    /**
     * Finds the cheapest currently-valid price for a product, i.e. the lowest-priced
     * snapshot that has no end date.
     *
     * @param productId the product identifier
     * @return the cheapest active snapshot, if any
     */
    Optional<PriceSnapshot> findFirstByProductIdAndDateEndIsNullOrderByPriceAsc(UUID productId);
}
