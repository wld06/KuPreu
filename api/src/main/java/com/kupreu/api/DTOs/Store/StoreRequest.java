package com.kupreu.api.DTOs.Store;

import lombok.Data;

import java.util.UUID;

/**
 * Request payload for creating or updating a store, referencing its chain and postal code.
 */
@Data
public class StoreRequest {
    private String address;
    private UUID supermarketChainId;
    private String postalCode;
}