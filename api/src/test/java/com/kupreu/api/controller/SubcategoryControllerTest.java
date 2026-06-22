package com.kupreu.api.controller;

import com.kupreu.api.audit.AuditService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.Category.CategoryResponse;
import com.kupreu.api.DTOs.Subcategory.SubcategoryWithCategory;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.SubcategoryService;

@WebMvcTest(controllers = SubcategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class SubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubcategoryService subcategoryService;

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

    private SubcategoryWithCategory sample() {
        return SubcategoryWithCategory.builder()
                .id(ID).name("Yogures")
                .category(CategoryResponse.builder().id(UUID.randomUUID()).name("Lácteos").build())
                .build();
    }

    // ── GET /api/subcategories/{id} ───────────────────────────────────────────

    @Test
    void getById_found_returns200() throws Exception {
        when(subcategoryService.getById(ID)).thenReturn(sample());

        mockMvc.perform(get("/api/subcategories/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yogures"))
                .andExpect(jsonPath("$.category.name").value("Lácteos"));
    }

    @Test
    void getById_notFound_returns500() throws Exception {
        when(subcategoryService.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("Subcategory with id " + ID + " does not exist"));

        mockMvc.perform(get("/api/subcategories/{id}", ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/subcategories — authorization ───────────────────────────────

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Yogures\",\"categoryId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/subcategories/{id} — authorization ───────────────────────────

    @Test
    void update_anonymous_returns403() throws Exception {
        mockMvc.perform(put("/api/subcategories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Postres\",\"categoryId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/subcategories/{id} ────────────────────────────────────────

    @Test
    void delete_anonymous_returns403() throws Exception {
        mockMvc.perform(delete("/api/subcategories/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/subcategories/{id}", ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_returns204() throws Exception {
        doNothing().when(subcategoryService).delete(ID);

        mockMvc.perform(delete("/api/subcategories/{id}", ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminRole_notFound_returns500() throws Exception {
        doThrow(new RuntimeException("Subcategory with id " + ID + " does not exist"))
                .when(subcategoryService).delete(any(UUID.class));

        mockMvc.perform(delete("/api/subcategories/{id}", ID))
                .andExpect(status().isInternalServerError());
    }
}
