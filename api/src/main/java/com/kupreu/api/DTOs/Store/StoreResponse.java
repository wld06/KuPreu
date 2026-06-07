package com.kupreu.api.DTOs.Store;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {
    public UUID id;
    public String address;
    public String chain;
}
