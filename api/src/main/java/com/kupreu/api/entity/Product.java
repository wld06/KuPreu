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

/**
 * JPA entity representing a catalog product.
 * A product is classified by subcategory, brand and unit of measure, and is
 * identified externally by its EAN barcode.
 */
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

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** EAN barcode that identifies the product externally; must be unique. */
    @Column(nullable = false, unique = true)
    private String ean;

    /** Display name of the product. */
    @Column(nullable = false)
    private String name;

    /** Available stock quantity; may be {@code null} when unknown. */
    @Column(name = "stock_qty")
    private Integer stockQty;

    /** Subcategory this product is classified under. */
    @ManyToOne
    @JoinColumn(name = "id_subcategory", nullable = false)
    @ToString.Exclude
    private Subcategory subcategory;

    /** Brand that manufactures or markets the product. */
    @ManyToOne
    @JoinColumn(name = "id_brand")
    @NonNull
    @ToString.Exclude
    private Brand brand;

    /** Unit of measure the product is sold in. */
    @ManyToOne
    @JoinColumn(name = "id_unit", nullable = false)
    @ToString.Exclude
    private UnitOfMeasure unitOfMeasure;
}
