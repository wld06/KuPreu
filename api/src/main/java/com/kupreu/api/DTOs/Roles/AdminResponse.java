package com.kupreu.api.DTOs.Roles;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminResponse {
    public UUID id;
    public String username;
    public boolean isAdmin;
}
