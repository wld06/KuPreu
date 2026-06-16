package com.kupreu.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.DateDIMDTO;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotRequest;
import com.kupreu.api.DTOs.PriceSnapshot.PriceSnapshotResponse;
import com.kupreu.api.DTOs.Store.StoreResponse;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.RateLimitFilter;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.PriceSnapshotService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = PriceSnapshotController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class PriceSnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private PriceSnapshotService priceSnapshotService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID PRODUCT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STORE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private PriceSnapshotResponse sample() {
        return PriceSnapshotResponse.builder()
                .uuid(PRODUCT_ID)
                .store(StoreResponse.builder().id(STORE_ID).address("Calle A").chain("Mercadona").build())
                .price(new BigDecimal("1.50"))
                .dateStart(DateDIMDTO.builder().id(UUID.randomUUID()).date(LocalDateTime.of(2026, 1, 1, 0, 0)).build())
                .dateEnd(DateDIMDTO.builder().id(UUID.randomUUID()).date(LocalDateTime.of(2026, 2, 1, 0, 0)).build())
                .build();
    }

    // ── GET /api/products/{id}/prices ─────────────────────────────────────────

    @Test
    void getByProductId_returnsList() throws Exception {
        when(priceSnapshotService.getPriceSnapshotByProductId(PRODUCT_ID)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/products/{id}/prices", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1.50))
                .andExpect(jsonPath("$[0].store.chain").value("Mercadona"));
    }

    @Test
    void getByProductId_withStoreId_delegatesToStoreQuery() throws Exception {
        when(priceSnapshotService.getPriceSnapshotsByProductIdAndStoreId(PRODUCT_ID, STORE_ID))
                .thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/products/{id}/prices", PRODUCT_ID).param("storeId", STORE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(priceSnapshotService).getPriceSnapshotsByProductIdAndStoreId(PRODUCT_ID, STORE_ID);
    }

    @Test
    void getByProductId_notFound_returns500() throws Exception {
        when(priceSnapshotService.getPriceSnapshotByProductId(any(UUID.class)))
                .thenThrow(new RuntimeException("Price snapshot not found for product id: " + PRODUCT_ID));

        mockMvc.perform(get("/api/products/{id}/prices", PRODUCT_ID))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/products/{id}/prices/cheapest ────────────────────────────────

    @Test
    void getCheapest_returns200() throws Exception {
        when(priceSnapshotService.getCheapest(PRODUCT_ID)).thenReturn(sample());

        mockMvc.perform(get("/api/products/{id}/prices/cheapest", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(1.50));
    }

    @Test
    void getCheapest_notFound_returns500() throws Exception {
        when(priceSnapshotService.getCheapest(any(UUID.class)))
                .thenThrow(new RuntimeException("No active price snapshots with product id: " + PRODUCT_ID));

        mockMvc.perform(get("/api/products/{id}/prices/cheapest", PRODUCT_ID))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/prices ──────────────────────────────────────────────────────
    // NOTE: PriceSnapshotController declares no @PreAuthorize, but SecurityConfig's
    // anyRequest().authenticated() means writes still require an authenticated user
    // (any role). There is no ADMIN restriction on creating price snapshots, unlike
    // the other write endpoints — see findings.

    @Test
    void create_anonymous_returns403() throws Exception {
        mockMvc.perform(post("/api/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "USER")
    void create_authenticated_returns200() throws Exception {
        PriceSnapshotRequest req = new PriceSnapshotRequest();
        req.setProductId(PRODUCT_ID);
        req.setStoreId(STORE_ID);
        req.setPrice(new BigDecimal("1.50"));
        req.setDateStart(LocalDateTime.of(2026, 1, 1, 0, 0));
        req.setDateEnd(LocalDateTime.of(2026, 2, 1, 0, 0));

        when(priceSnapshotService.create(any(PriceSnapshotRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(1.50));
    }
}
