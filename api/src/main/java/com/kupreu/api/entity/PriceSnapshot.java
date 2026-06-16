package com.kupreu.api.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "price_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot {

    @EmbeddedId
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
    private Product product;

    @ManyToOne
    @MapsId("storeId")
    @JoinColumn(name = "id_store", nullable = false)
    private Store store;

    @ManyToOne
    @MapsId("dateStartId")
    @JoinColumn(name = "id_date_start", nullable = false)
    private DateDIM dateStart;

    @ManyToOne
    @MapsId("dateEndId")
    @JoinColumn(name = "id_date_end", nullable = true)
    private DateDIM dateEnd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
