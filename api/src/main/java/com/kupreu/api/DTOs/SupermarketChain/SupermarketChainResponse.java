package com.kupreu.api.DTOs.SupermarketChain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response payload representing a supermarket chain summary, including its store count.
 */
@Data
@Builder
public class SupermarketChainResponse {
    private UUID id;
    private String name;
    private long storeCount;
}
