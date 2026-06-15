package com.kupreu.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.Attribute.AttributeRequest;
import com.kupreu.api.DTOs.Attribute.AttributeResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.AttributeService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AttributeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class AttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private AttributeService service;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private AttributeResponse sample() {
        return AttributeResponse.builder().id(ID).name("Color").build();
    }

    private AttributeRequest request(String name) {
        AttributeRequest req = new AttributeRequest();
        req.setName(name);
        return req;
    }

    // ── GET /api/attributes ─────────────────────────────────────────────────────

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/attributes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Color"));
    }

    // ── GET /api/attributes/{id} ──────────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(service.getById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/attributes/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Color"));
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(service.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("Attribute not found"));

        mockMvc.perform(get("/api/attributes/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/attributes ──────────────────────────────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Color"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Color"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminRole_returns200() throws Exception {
        when(service.create(any(AttributeRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Color"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Color"));
    }

    // ── PUT /api/attributes/{id} ──────────────────────────────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        mockMvc.perform(put("/api/attributes/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Material"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_userRole_returns403() throws Exception {
        mockMvc.perform(put("/api/attributes/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Material"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_returns200() throws Exception {
        AttributeResponse updated = AttributeResponse.builder().id(ID).name("Material").build();
        when(service.update(eq(ID), any(AttributeRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/attributes/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Material"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Material"));
    }

    // ── DELETE /api/attributes/{id} ───────────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/attributes/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/attributes/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_returns204() throws Exception {
        doNothing().when(service).delete(ID);

        mockMvc.perform(delete("/api/attributes/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Attribute not found"))
                .when(service).delete(any(UUID.class));

        mockMvc.perform(delete("/api/attributes/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
