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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.Product.ProductRequest;
import com.kupreu.api.DTOs.Product.ProductResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.ProductService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private ProductResponse sample() {
        return ProductResponse.builder()
                .id(ID).ean("1234567890123").name("Leche").stockQty(10)
                .subcategoryName("Yogures").brandName("Hacendado").unitOfMeasureName("Litro")
                .build();
    }

    private ProductRequest request(String name) {
        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setEan("1234567890123");
        req.setStock(10);
        req.setSubcategoryId(UUID.randomUUID());
        req.setBrandId(UUID.randomUUID());
        req.setUnitOfMeasureId(UUID.randomUUID());
        return req;
    }

    // ── GET /api/products ─────────────────────────────────────────────────────

    @Test
    void getAll_returnsPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductResponse> page = new PageImpl<>(List.of(sample()), pageable, 1);
        when(productService.getProducts(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Leche"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ── GET /api/products/{id} ────────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(productService.getProductById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/products/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Leche"));
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(productService.getProductById(any(UUID.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/products/ean/{ean} ───────────────────────────────────────────

    @Test
    void getByEan_found_returns200() throws Exception {
        when(productService.getByEan("1234567890123")).thenReturn(sample());

        mockMvc.perform(get("/api/products/ean/{ean}", "1234567890123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ean").value("1234567890123"));
    }

    // ── POST /api/products ────────────────────────────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Leche"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Leche"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminRole_returns200() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Leche"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Leche"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/products/{id} ────────────────────────────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        mockMvc.perform(put("/api/products/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Leche Entera"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_returns200() throws Exception {
        when(productService.update(eq(ID), any(ProductRequest.class))).thenReturn(sample());

        mockMvc.perform(put("/api/products/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("Leche Entera"))))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_returns204() throws Exception {
        doNothing().when(productService).delete(ID);

        mockMvc.perform(delete("/api/products/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Product not found"))
                .when(productService).delete(any(UUID.class));

        mockMvc.perform(delete("/api/products/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
