package com.kupreu.api.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    private String ean;

    @Column(nullable = false)
    private String name;

    @Column(name = "stock_qty")
    private Integer stockQty;

    @ManyToOne
    @JoinColumn(name = "id_subcategory", nullable = false)
    @ToString.Exclude
    private Subcategory subcategory;

    @ManyToOne
    @JoinColumn(name = "id_brand")
    @NonNull
    @ToString.Exclude
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "id_unit", nullable = false)
    @ToString.Exclude
    private UnitOfMeasure unitOfMeasure;
}
