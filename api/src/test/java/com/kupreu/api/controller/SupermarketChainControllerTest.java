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

import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainRequest;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainResponse;
import com.kupreu.api.DTOs.SupermarketChain.SupermarketChainWithStoresResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.SupermarketChainService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = SupermarketChainController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class SupermarketChainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private SupermarketChainService service;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private SupermarketChainWithStoresResponse sample() {
        return SupermarketChainWithStoresResponse.builder().id(ID).name("Mercadona").stores(List.of()).build();
    }

    private SupermarketChainRequest request(String name) {
        SupermarketChainRequest req = new SupermarketChainRequest();
        req.setName(name);
        return req;
    }

    // ── GET /api/chains ───────────────────────────────────────────────────────

    @Test
    void getAll_returnsList() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                SupermarketChainResponse.builder().id(ID).name("Mercadona").storeCount(3).build()));

        mockMvc.perform(get("/api/chains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mercadona"))
                .andExpect(jsonPath("$[0].storeCount").value(3));
    }

    // ── GET /api/chains/{id} ──────────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(service.getById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/chains/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mercadona"))
                .andExpect(jsonPath("$.stores").isArray());
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(service.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("Supermarket chain not found"));

        mockMvc.perform(get("/api/chains/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/chains ──────────────────────────────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/chains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Mercadona"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/chains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Mercadona"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminRole_returns200() throws Exception {
        when(service.create(any(SupermarketChainRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/chains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Mercadona"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mercadona"));
    }

    // ── PUT /api/chains/{id} ──────────────────────────────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        mockMvc.perform(put("/api/chains/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Mercadona Online"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_returns200() throws Exception {
        SupermarketChainWithStoresResponse updated = SupermarketChainWithStoresResponse.builder()
                .id(ID).name("Mercadona Online").stores(List.of()).build();
        when(service.update(eq(ID), any(SupermarketChainRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/chains/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Mercadona Online"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mercadona Online"));
    }

    // ── DELETE /api/chains/{id} ───────────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/chains/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_returns204() throws Exception {
        doNothing().when(service).delete(ID);

        mockMvc.perform(delete("/api/chains/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Supermarket chain not found"))
                .when(service).delete(any(UUID.class));

        mockMvc.perform(delete("/api/chains/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
