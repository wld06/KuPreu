package com.kupreu.api.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for {@link PriceSnapshot}.
 * A snapshot is uniquely identified by the product, the store, and the date
 * the price started being valid.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshotId implements Serializable {

    /** Identifier of the product the price refers to. */
    @Column(name = "id_product")
    private UUID productId;

    /** Identifier of the store where the price was observed. */
    @Column(name = "id_store")
    private UUID storeId;

    /** Identifier of the date dimension marking when the price became valid. */
    @Column(name = "id_date_start")
    private UUID dateStartId;
}
