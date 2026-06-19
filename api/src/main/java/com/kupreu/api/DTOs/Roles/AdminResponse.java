package com.kupreu.api.DTOs.Roles;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminResponse {
    private UUID id;
    private String username;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
}
