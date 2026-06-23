package com.kupreu.api.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a top-level product category.
 * Each category groups one or more {@link Subcategory} entries.
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID Id;

    /** Human-readable category name; must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Subcategories that belong to this category. */
    @OneToMany(mappedBy = "category")
    @ToString.Exclude
    private List<Subcategory> subcategories;
}
