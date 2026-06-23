package com.kupreu.api.DTOs.SupermarketChain;

import com.kupreu.api.DTOs.Store.StoreResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Response payload representing a supermarket chain together with its stores.
 */
@Data
@Builder
public class SupermarketChainWithStoresResponse {
    private UUID id;
    private String name;
    private List<StoreResponse> stores;
}
