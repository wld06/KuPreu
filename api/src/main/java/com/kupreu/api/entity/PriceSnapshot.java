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

    @EmbeddedId
    @EqualsAndHashCode.Include
    private PriceSnapshotId id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @PrePersist
    private void assignUuid(){
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "id_product", nullable = false)
    @ToString.Exclude
    private Product product;

    @ManyToOne
    @MapsId("storeId")
    @JoinColumn(name = "id_store", nullable = false)
    @ToString.Exclude
    private Store store;

    @ManyToOne
    @MapsId("dateStartId")
    @JoinColumn(name = "id_date_start", nullable = false)
    @ToString.Exclude
    private DateDIM dateStart;

    @ManyToOne
    @JoinColumn(name = "id_date_end", nullable = true)
    @ToString.Exclude
    private DateDIM dateEnd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
