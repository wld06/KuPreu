package com.kupreu.api.DTOs.Store;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {
    private UUID id;
    private String address;
    private String chain;
}
