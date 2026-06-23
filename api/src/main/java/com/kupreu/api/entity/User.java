package com.kupreu.api.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * JPA entity representing an application user account.
 * Holds credentials, profile data and the user's shopping lists.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** Login name; must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String username;

    /** User's first name. */
    @Column(nullable = false)
    private String name;

    /** User's last name. */
    @Column(nullable = false)
    private String surname;

    /** BCrypt-hashed password; excluded from {@code toString} to avoid leaking it. */
    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    /** Contact e-mail; must be unique and non-null. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Whether the user has administrator privileges. */
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    /** Timestamp set when the account is created; defaults to the current time. */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Postal code the user is associated with; optional. */
    @ManyToOne
    @JoinColumn(name = "id_postal_code")
    @ToString.Exclude
    private PostalCode postalCode;

    /** Shopping lists owned by this user. */
    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<ShoppingList> shoppingLists;
}
