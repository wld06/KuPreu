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
 * JPA entity representing a postal code and its associated city.
 * Used to locate both {@link Store} branches and {@link User} accounts.
 */
@Entity
@Table(name = "postal_code")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostalCode {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** The postal code value; must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String code;

    /** Name of the city this postal code belongs to. */
    @Column(nullable = false)
    private String city;

    /** Stores located within this postal code. */
    @OneToMany(mappedBy = "postalCode")
    @ToString.Exclude
    private List<Store> stores;

    /** Users registered within this postal code. */
    @OneToMany(mappedBy = "postalCode")
    @ToString.Exclude
    private List<User> users;
}
