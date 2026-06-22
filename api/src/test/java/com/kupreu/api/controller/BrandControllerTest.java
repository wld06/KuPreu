package com.kupreu.api.controller;

import com.kupreu.api.audit.AuditService;

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

import com.kupreu.api.DTOs.Brand.BrandRequest;
import com.kupreu.api.DTOs.Brand.BrandResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.BrandService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = BrandController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    @SuppressWarnings("unused")
    private AuditService auditService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private BrandResponse sample() {
        return BrandResponse.builder().id(ID).name("Hacendado").build();
    }

    private BrandRequest request(String name) {
        BrandRequest req = new BrandRequest();
        req.setName(name);
        return req;
    }

    // ── GET /api/brands ───────────────────────────────────────────────────────

    @Test
    void getAll_returnsList() throws Exception {
        when(brandService.getAll(null)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Hacendado"));
    }

    @Test
    void getAll_withFilter_passesParam() throws Exception {
        when(brandService.getAll("Pascual")).thenReturn(List.of(
                BrandResponse.builder().id(ID).name("Pascual").build()));

        mockMvc.perform(get("/api/brands").param("brandName", "Pascual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pascual"));
    }

    // ── GET /api/brands/{id} ──────────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(brandService.getById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/brands/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hacendado"));
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(brandService.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("Brand with id" + ID + " not found"));

        mockMvc.perform(get("/api/brands/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/brands ──────────────────────────────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Hacendado"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Hacendado"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminRole_returns200() throws Exception {
        when(brandService.create(any(BrandRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Hacendado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hacendado"));
    }

    // ── PUT /api/brands/{id} ──────────────────────────────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        mockMvc.perform(put("/api/brands/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Hacendado Premium"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_returns200() throws Exception {
        BrandResponse updated = BrandResponse.builder().id(ID).name("Hacendado Premium").build();
        when(brandService.update(eq(ID), any(BrandRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/brands/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Hacendado Premium"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hacendado Premium"));
    }

    // ── DELETE /api/brands/{id} ───────────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/brands/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_returns204() throws Exception {
        doNothing().when(brandService).delete(ID);

        mockMvc.perform(delete("/api/brands/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Brand not found"))
                .when(brandService).delete(any(UUID.class));

        mockMvc.perform(delete("/api/brands/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
