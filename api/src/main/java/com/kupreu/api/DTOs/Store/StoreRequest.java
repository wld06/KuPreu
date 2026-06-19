package com.kupreu.api.DTOs.Store;

import lombok.Data;

import java.util.UUID;

@Data
public class StoreRequest {
    private String address;
    private UUID supermarketChainId;
    private String postalCode;
}