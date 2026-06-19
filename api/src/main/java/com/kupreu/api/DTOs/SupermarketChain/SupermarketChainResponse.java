package com.kupreu.api.DTOs.SupermarketChain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SupermarketChainResponse {
    private UUID id;
    private String name;
    private long storeCount;
}
