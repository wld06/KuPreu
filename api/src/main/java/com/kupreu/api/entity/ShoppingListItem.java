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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a single line in a {@link ShoppingList}.
 * Each item links a {@link PriceSnapshot} (a specific product/store/price) with a quantity.
 */
@Entity
@Table(name = "shopping_list_item")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItem {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** Number of units of the product to buy. */
    @Column(nullable = false)
    private Integer quantity;

    /** Shopping list this item belongs to. */
    @ManyToOne
    @JoinColumn(name = "id_list", nullable = false)
    @ToString.Exclude
    private ShoppingList shoppingList;

    /** Price snapshot (product, store and price) chosen for this item. */
    @ManyToOne
    @JoinColumn(name = "price_snapshot_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    private PriceSnapshot priceSnapshot;
}
