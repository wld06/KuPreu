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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supermarket_chain")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupermarketChain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "supermarketChain")
    private List<Store> supermarkets;
}
