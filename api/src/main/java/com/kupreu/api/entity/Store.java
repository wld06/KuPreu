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
 * JPA entity representing a physical store belonging to a {@link SupermarketChain}.
 */
@Entity
@Table(name = "store")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** Street address of the store. */
    @Column(nullable = false)
    private String address;

    /** Supermarket chain this store belongs to. */
    @ManyToOne
    @JoinColumn(name = "id_chain", nullable = false)
    @ToString.Exclude
    private SupermarketChain supermarketChain;

    /** Postal code where the store is located; optional. */
    @ManyToOne
    @JoinColumn(name = "id_postal_code")
    @ToString.Exclude
    private PostalCode postalCode;
}
