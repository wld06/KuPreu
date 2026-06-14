package com.kupreu.api.DTOs.SupermarketChain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SupermarketChainResponse {
    public UUID id;
    public String name;
    public long storeCount;
}
