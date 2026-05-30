package com.kupreu.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kupreu.api.DTOs.AuthResponse;
import com.kupreu.api.DTOs.LoginRequest;
import com.kupreu.api.DTOs.RegisterRequest;
import com.kupreu.api.config.security.JwtAuthFilter;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.config.security.SecurityConfig;
import com.kupreu.api.service.AuthService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtProvider jwtProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserDetailsService userDetailsService;

    private static final UUID FAKE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String FAKE_TOKEN = "header.payload.signature";

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        RegisterRequest req = new RegisterRequest("user@test.com", "Ana", "García", "ana", "pass123");
        AuthResponse res = AuthResponse.builder()
                .id(FAKE_ID).token(FAKE_TOKEN)
                .email("user@test.com").username("ana")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(FAKE_TOKEN))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.id").value(FAKE_ID.toString()));
    }

    @Test
    void register_duplicateEmail_returns500() throws Exception {
        RegisterRequest req = new RegisterRequest("dup@test.com", "Ana", "García", "ana", "pass123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void register_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest("user@test.com", "pass123");
        AuthResponse res = AuthResponse.builder()
                .id(FAKE_ID).token(FAKE_TOKEN)
                .email("user@test.com").username("ana")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(FAKE_TOKEN))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void login_badCredentials_returns500() throws Exception {
        LoginRequest req = new LoginRequest("user@test.com", "wrongpass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_noContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .content("{\"email\":\"a@b.com\",\"password\":\"x\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
