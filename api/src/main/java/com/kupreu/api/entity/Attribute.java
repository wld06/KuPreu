package com.kupreu.api.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a product attribute (for example "Organic" or "Gluten-free").
 * Attribute names are unique across the catalog.
 */
@Entity
@Table(name = "attribute")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID Id;

    /** Human-readable attribute name; must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String name;
}