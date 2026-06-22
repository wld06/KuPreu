package com.kupreu.api.controller;

import com.kupreu.api.audit.AuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.Profile.PasswordRequest;
import com.kupreu.api.DTOs.Profile.ProfileRequest;
import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.Users.ProfileService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ProfileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    @SuppressWarnings("unused")
    private AuditService auditService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private ProfileResponse sample() {
        return ProfileResponse.builder()
                .id(ID).username("ana").name("Ana").surname("García")
                .email("ana@test.com").build();
    }

    // ── GET /api/profile/me ───────────────────────────────────────────────────

    @Test
    void getMyProfile_anonymous_returns403() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "USER")
    void getMyProfile_authenticated_returns200() throws Exception {
        when(profileService.getMyProfile("ana@test.com")).thenReturn(sample());

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.email").value("ana@test.com"))
                .andExpect(jsonPath("$.username").value("ana"));
    }

    @Test
    @WithMockUser(username = "ghost@test.com", roles = "USER")
    void getMyProfile_userNotFound_returns500() throws Exception {
        when(profileService.getMyProfile(anyString()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/profile/{id} (ADMIN) ─────────────────────────────────────────

    @Test
    void getProfileById_anonymous_returns403() throws Exception {
        mockMvc.perform(get("/api/profile/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProfileById_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/profile/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileById_adminRole_returns200() throws Exception {
        when(profileService.getProfileById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/profile/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileById_notFound_returns500() throws Exception {
        when(profileService.getProfileById(any(UUID.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/profile/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/profile (by email, ADMIN) ────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileByEmail_adminRole_returns200() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setEmail("ana@test.com");

        when(profileService.getProfileByEmail("ana@test.com")).thenReturn(sample());

        mockMvc.perform(get("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProfileByEmail_userRole_returns403() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setEmail("ana@test.com");

        mockMvc.perform(get("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileByEmail_invalidEmail_returns400() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setEmail("not-an-email");

        mockMvc.perform(get("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/profile/update/password ──────────────────────────────────────

    @Test
    void updatePassword_anonymous_returns403() throws Exception {
        PasswordRequest req = new PasswordRequest();
        req.setNewPassword("NewPass1!");

        mockMvc.perform(put("/api/profile/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "USER")
    void updatePassword_validRequest_returns200() throws Exception {
        PasswordRequest req = new PasswordRequest();
        req.setNewPassword("NewPass1!");
        req.setActualPassword("OldPass1!");

        doNothing().when(profileService).updatePassword(any(UserDetails.class), any(PasswordRequest.class));

        mockMvc.perform(put("/api/profile/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"));
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "USER")
    void updatePassword_weakPassword_returns400() throws Exception {
        PasswordRequest req = new PasswordRequest();
        req.setNewPassword("weak");

        mockMvc.perform(put("/api/profile/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "ana@test.com", roles = "USER")
    void updatePassword_missingBody_returns400() throws Exception {
        mockMvc.perform(put("/api/profile/update/password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "ghost@test.com", roles = "USER")
    void updatePassword_userNotFound_returns500() throws Exception {
        PasswordRequest req = new PasswordRequest();
        req.setNewPassword("NewPass1!");
        req.setActualPassword("OldPass1!");

        doThrow(new RuntimeException("User not found"))
                .when(profileService).updatePassword(any(UserDetails.class), any(PasswordRequest.class));

        mockMvc.perform(put("/api/profile/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}
