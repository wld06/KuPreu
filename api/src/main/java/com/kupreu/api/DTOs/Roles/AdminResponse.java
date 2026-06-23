package com.kupreu.api.DTOs.Roles;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * Response payload returned after a role change, reflecting the user's current admin status.
 */
@Data
@Builder
public class AdminResponse {
    private UUID id;
    private String username;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
}
