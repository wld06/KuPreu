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

import com.kupreu.api.DTOs.Category.CategoryRequest;
import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Category.CategoryWithSubcategoriesResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.CategoryService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private CategoryService categoryService;

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

    private CategoryResponse sample() {
        return CategoryResponse.builder().id(ID).name("Lácteos").build();
    }

    private CategoryWithSubcategoriesResponse sampleWithSubcategories() {
        return CategoryWithSubcategoriesResponse.builder().id(ID).name("Lácteos").subcategories(List.of()).build();
    }

    // ── GET /api/categories ───────────────────────────────────────────────────

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(
                CategoryWithSubcategoriesResponse.builder().id(ID).name("Lácteos").subcategories(List.of()).build(),
                CategoryWithSubcategoriesResponse.builder().id(UUID.randomUUID()).name("Bebidas").subcategories(List.of()).build()
        ));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Lácteos"))
                .andExpect(jsonPath("$[1].name").value("Bebidas"));
    }

    // ── GET /api/categories/{id} ──────────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(categoryService.getById(ID)).thenReturn(sampleWithSubcategories());

        mockMvc.perform(get("/api/categories/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Lácteos"))
                .andExpect(jsonPath("$.subcategories").isArray());
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(categoryService.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("Category not found with id: " + ID));

        mockMvc.perform(get("/api/categories/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/categories ──────────────────────────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userRole_returns403() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminRole_returns201() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos");

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Lácteos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_duplicateName_returns500() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos");

        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category already exists with name: Lácteos"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/categories/{id} ──────────────────────────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos y Huevos");

        mockMvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_found_returns200() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Lácteos y Huevos");

        CategoryResponse updated = CategoryResponse.builder().id(ID).name("Lácteos y Huevos").build();
        when(categoryService.update(eq(ID), any(CategoryRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lácteos y Huevos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_adminRole_notFound_returns500() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("X");

        when(categoryService.update(any(UUID.class), any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    // ── DELETE /api/categories/{id} ───────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_found_returns204() throws Exception {
        doNothing().when(categoryService).delete(ID);

        mockMvc.perform(delete("/api/categories/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Category not found"))
                .when(categoryService).delete(any(UUID.class));

        mockMvc.perform(delete("/api/categories/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
