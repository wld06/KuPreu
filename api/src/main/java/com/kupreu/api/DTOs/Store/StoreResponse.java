package com.kupreu.api.DTOs.Store;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload representing a store, with its chain flattened to a name.
 */
@Data
@Builder
public class StoreResponse {
    private UUID id;
    private String address;
    private String chain;
}
