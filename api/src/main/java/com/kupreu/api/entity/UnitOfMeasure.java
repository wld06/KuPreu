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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unit_of_measure")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;
    
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String symbol;
}