package com.kupreu.api.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing a user's shopping list.
 * A list name is unique per user, and removing a list cascades to its items.
 */
@Entity
@Table(name = "shopping_list",
       uniqueConstraints = @UniqueConstraint(name = "uq_shopping_list_user_name", columnNames = {"id_user", "name"}))
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingList {

    /** Surrogate primary key, generated as a random UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    /** Display name of the list; unique within the owning user. */
    @Column(nullable = false)
    private String name;

    /** Timestamp set when the list is created; defaults to the current time. */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** User who owns this list. */
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    @ToString.Exclude
    private User user;

    /** Items contained in this list; removed together with the list. */
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ShoppingListItem> items;
}
