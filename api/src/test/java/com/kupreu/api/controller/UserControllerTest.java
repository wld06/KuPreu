package com.kupreu.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.Profile.ProfileResponse;
import com.kupreu.api.DTOs.Roles.AdminRequest;
import com.kupreu.api.DTOs.Roles.AdminResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.Users.UserService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private ProfileResponse profile() {
        return ProfileResponse.builder()
                .id(ID).username("ana").email("ana@test.com").build();
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAllUsers_anonymous_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_adminRole_returnsPage() throws Exception {
        Page<ProfileResponse> page = new PageImpl<>(List.of(profile()), PageRequest.of(0, 20), 1);
        when(userService.getAllUsers(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("ana"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_customPaging_passesParams() throws Exception {
        Page<ProfileResponse> page = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
        when(userService.getAllUsers(2, 5)).thenReturn(page);

        mockMvc.perform(get("/api/users").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_emptyPostalCode_returns500() throws Exception {
        // toProfileResponse dereferences postalCode; null triggers NPE -> 500
        when(userService.getAllUsers(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("postalCode is null"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError());
    }

    // ── DELETE /api/users/{id} ────────────────────────────────────────────────

    @Test
    void deleteUser_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_adminRole_returns204() throws Exception {
        doNothing().when(userService).deleteUser(ID);

        mockMvc.perform(delete("/api/users/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_serviceThrows_returns500() throws Exception {
        doThrow(new RuntimeException("boom")).when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/users/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── PUT /api/users/{id}/role ──────────────────────────────────────────────

    @Test
    void updateUserRole_anonymous_returns403() throws Exception {
        AdminRequest req = new AdminRequest();
        req.setAdmin(true);

        mockMvc.perform(put("/api/users/{id}/role", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserRole_userRole_returns403() throws Exception {
        AdminRequest req = new AdminRequest();
        req.setAdmin(true);

        mockMvc.perform(put("/api/users/{id}/role", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_grantAdmin_returns200() throws Exception {
        AdminRequest req = new AdminRequest();
        req.setAdmin(true);

        AdminResponse res = AdminResponse.builder().id(ID).username("ana").isAdmin(true).build();
        when(userService.updateUserRole(eq(ID), eq(true))).thenReturn(res);

        mockMvc.perform(put("/api/users/{id}/role", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.isAdmin").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_revokeAdmin_returns200() throws Exception {
        AdminRequest req = new AdminRequest();
        req.setAdmin(false);

        AdminResponse res = AdminResponse.builder().id(ID).username("ana").isAdmin(false).build();
        when(userService.updateUserRole(eq(ID), eq(false))).thenReturn(res);

        mockMvc.perform(put("/api/users/{id}/role", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_userNotFound_returns500() throws Exception {
        AdminRequest req = new AdminRequest();
        req.setAdmin(true);

        when(userService.updateUserRole(any(UUID.class), eq(true)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/{id}/role", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
}
