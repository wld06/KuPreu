package com.kupreu.api.service;

import com.kupreu.api.audit.AuditService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kupreu.api.DTOs.AuthResponse;
import com.kupreu.api.DTOs.LoginRequest;
import com.kupreu.api.DTOs.RegisterRequest;
import com.kupreu.api.config.security.JwtProvider;
import com.kupreu.api.entity.User;
import com.kupreu.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private AuthenticationManager authenticationManager;

    @Mock private AuditService auditService;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerReq() {
        return new RegisterRequest("ana@test.com", "Ana", "García", "ana", "Pass123!");
    }

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        RegisterRequest req = registerReq();
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("hashed");
        when(jwtProvider.generateToken("ana@test.com")).thenReturn("jwt-token");

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getEmail()).isEqualTo("ana@test.com");
        assertThat(res.getUsername()).isEqualTo("ana");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_storesHashedPasswordAndNonAdmin() {
        RegisterRequest req = registerReq();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Pass123!")).thenReturn("hashed");
        when(jwtProvider.generateToken(anyString())).thenReturn("jwt-token");

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        authService.register(req);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.isAdmin()).isFalse();
        assertThat(saved.getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    void register_duplicateEmail_throwsAndDoesNotSave() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerReq()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest req = new LoginRequest("ana@test.com", "Pass123!");
        User user = User.builder()
                .id(UUID.randomUUID()).email("ana@test.com").username("ana")
                .password("hashed").build();

        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        when(jwtProvider.generateToken("ana@test.com")).thenReturn("jwt-token");

        AuthResponse res = authService.login(req);

        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getUsername()).isEqualTo("ana");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequest req = new LoginRequest("ana@test.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_authPassesButUserMissing_throws() {
        LoginRequest req = new LoginRequest("ana@test.com", "Pass123!");
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
