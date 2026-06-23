package com.kupreu.api.DTOs.Roles;

import lombok.Data;

/**
 * Request payload carrying the desired admin flag when changing a user's role.
 */
@Data
public class AdminRequest {
    private boolean isAdmin;
}
