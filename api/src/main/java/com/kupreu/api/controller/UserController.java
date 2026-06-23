package com.kupreu.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.DTOs.Roles.AdminRequest;
import com.kupreu.api.DTOs.Roles.AdminResponse;
import com.kupreu.api.service.Users.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing administrative user-management endpoints under
 * {@code /api/users}. Every endpoint requires the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    //Only admins can access this endpoints to manage users

    /**
     * Returns a paginated list of all users. Requires the {@code ADMIN} role.
     *
     * @param page zero-based page index
     * @param size page size
     * @return HTTP 200 with a page of user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProfileResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        Page<ProfileResponse> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Deletes a user account. Requires the {@code ADMIN} role.
     *
     * @param id the user identifier
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Grants or revokes admin privileges for a user. Requires the {@code ADMIN} role.
     *
     * @param id      the user identifier
     * @param request the validated role data (admin flag)
     * @return HTTP 200 with the updated role information
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody AdminRequest request) {
        AdminResponse response = userService.updateUserRole(id, request.isAdmin());
        return ResponseEntity.ok(response);
    }
}
