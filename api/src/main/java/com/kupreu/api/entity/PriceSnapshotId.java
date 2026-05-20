package com.kupreu.api.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshotId implements Serializable {

    @Column(name = "id_product")
    private UUID productId;

    @Column(name = "id_store")
    private UUID storeId;

    @Column(name = "id_date_start")
    private UUID dateStartId;

    @Column(name = "id_date_end")
    private UUID dateEndId;
}
