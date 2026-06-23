package com.kupreu.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kupreu.api.DTOs.Profile.PasswordRequest;
import com.kupreu.api.DTOs.Profile.ProfileRequest;
import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.service.Users.ProfileService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

/**
 * REST controller exposing profile endpoints under {@code /api/profile}.
 * {@code /me} and password change act on the authenticated user; lookups by id or
 * e-mail require the {@code ADMIN} role.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    /**
     * Returns the profile of the authenticated user.
     *
     * @param userDetails the authenticated principal
     * @return HTTP 200 with the current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(profileService.getMyProfile(userDetails.getUsername()));
    }

    /**
     * Returns a user's profile by id. Requires the {@code ADMIN} role.
     *
     * @param id the user identifier
     * @return HTTP 200 with the profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable UUID id){
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    /**
     * Returns a user's profile by e-mail. Requires the {@code ADMIN} role.
     *
     * @param request the request carrying the target e-mail
     * @return HTTP 200 with the profile
     */
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> getProfileByEmail(@Valid @RequestBody ProfileRequest request){
        return ResponseEntity.ok(profileService.getProfileByEmail(request.getEmail()));
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param userDetails the authenticated principal
     * @param request     the current and new passwords
     * @return HTTP 200 with a confirmation message
     */
    @PutMapping("/update/password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody PasswordRequest request){
        profileService.updatePassword(userDetails, request);
        return ResponseEntity.ok("Updated");
    }

}
