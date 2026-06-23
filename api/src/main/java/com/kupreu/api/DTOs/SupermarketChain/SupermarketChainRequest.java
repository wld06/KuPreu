package com.kupreu.api.DTOs.SupermarketChain;

import lombok.Data;

/**
 * Request payload for creating or updating a supermarket chain.
 */
@Data
public class SupermarketChainRequest {
    private String name;
}
