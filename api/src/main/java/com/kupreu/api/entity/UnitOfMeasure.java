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
 * JPA entity representing a unit of measure for products (for example "Kilogram"/"kg").
 */
@Entity
@Table(name = "unit_of_measure")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID Id;

    /** Full unit name (for example "Kilogram"); must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Short symbol for the unit (for example "kg"). */
    @Column(nullable = false)
    private String symbol;
}