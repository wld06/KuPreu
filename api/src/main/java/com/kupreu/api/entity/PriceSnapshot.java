package com.kupreu.api.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
    @JoinColumn(name = "id_date_end", nullable = false)
    private DateDIM dateEnd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
