package com.kupreu.api.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity capturing the price of a product at a given store during a time window.
 * <p>
 * The natural key is the {@link PriceSnapshotId} composite (product + store + start date).
 * A separate {@code uuid} is also kept so that other entities (such as shopping list
 * items) can reference a snapshot through a single column.
 */
@Entity
@Table(name = "price_snapshot")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot {

    /** Composite primary key (product, store, start date). */
    @EmbeddedId
    @EqualsAndHashCode.Include
    private PriceSnapshotId id;

    /** Stable surrogate identifier used by entities that reference this snapshot. */
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /** Generates the surrogate {@code uuid} before the row is first persisted. */
    @PrePersist
    private void assignUuid(){
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    /** Product whose price this snapshot records. */
    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "id_product", nullable = false)
    @ToString.Exclude
    private Product product;

    /** Store where the price was observed. */
    @ManyToOne
    @MapsId("storeId")
    @JoinColumn(name = "id_store", nullable = false)
    @ToString.Exclude
    private Store store;

    /** Date from which this price became valid. */
    @ManyToOne
    @MapsId("dateStartId")
    @JoinColumn(name = "id_date_start", nullable = false)
    @ToString.Exclude
    private DateDIM dateStart;

    /** Date the price stopped being valid; {@code null} while the price is current. */
    @ManyToOne
    @JoinColumn(name = "id_date_end", nullable = true)
    @ToString.Exclude
    private DateDIM dateEnd;

    /** The price amount, stored with two decimal places. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
