package com.kupreu.api.DTOs.Store;

import lombok.Data;

import java.util.UUID;

@Data
public class StoreRequest {
    public String address;
    public UUID supermarketChainId;
    public String postalCode;
}