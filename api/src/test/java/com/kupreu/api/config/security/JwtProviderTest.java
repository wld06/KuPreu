package com.kupreu.api.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtProviderTest {

    // 64-byte secret, valid for HS256
    private static final String SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbm90LWZvci1wcm9kLXVzZS1wbGVhc2UtcmVwbGFjZS10aGlzLXRlc3Q=";

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", SECRET);
        ReflectionTestUtils.setField(jwtProvider, "expiration", 86_400_000L);
    }

    @Test
    void generateToken_thenExtractEmail_roundTrips() {
        String token = jwtProvider.generateToken("ana@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtProvider.extractEmail(token)).isEqualTo("ana@test.com");
    }

    @Test
    void isTokenValid_matchingEmail_returnsTrue() {
        String token = jwtProvider.generateToken("ana@test.com");

        assertThat(jwtProvider.isTokenValid(token, "ana@test.com")).isTrue();
    }

    @Test
    void isTokenValid_differentEmail_returnsFalse() {
        String token = jwtProvider.generateToken("ana@test.com");

        assertThat(jwtProvider.isTokenValid(token, "other@test.com")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtProvider, "expiration", -1_000L);
        String expired = jwtProvider.generateToken("ana@test.com");

        // expired tokens throw on parse (signature valid, but expired)
        assertThatThrownBy(() -> jwtProvider.isTokenValid(expired, "ana@test.com"))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void extractEmail_tamperedToken_throws() {
        String token = jwtProvider.generateToken("ana@test.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtProvider.extractEmail(tampered))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void extractEmail_garbageToken_throws() {
        assertThatThrownBy(() -> jwtProvider.extractEmail("not.a.jwt"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
