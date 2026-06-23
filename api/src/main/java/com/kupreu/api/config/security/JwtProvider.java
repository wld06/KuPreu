package com.kupreu.api.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Stateless helper that creates and validates signed JWTs.
 * Tokens are signed with an HMAC-SHA key derived from the configured secret and
 * carry the user's e-mail as the subject claim.
 */
@Component
public class JwtProvider {

    /** Base64/raw secret used to derive the HMAC signing key. */
    @Value("${jwt.secret}")
    private String secret;

    /** Token lifetime in milliseconds, added to the issue time to compute expiry. */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Derives the HMAC-SHA signing key from the configured secret.
     *
     * @return the key used to sign and verify tokens
     */
    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed token for the given user.
     *
     * @param email the user's e-mail, stored as the token subject
     * @return the compact, signed JWT string
     */
    public String generateToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the user's e-mail from the token's subject claim.
     *
     * @param token the signed JWT
     * @return the e-mail stored as the subject
     */
    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }

    /**
     * Checks that the token belongs to the given user and has not expired.
     *
     * @param token the signed JWT
     * @param email the e-mail expected to match the token subject
     * @return {@code true} if the subject matches and the token is still valid
     */
    public boolean isTokenValid(String token, String email){
        String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isExpired(token);
    }

    /**
     * Tells whether the token's expiry date is in the past.
     *
     * @param token the signed JWT
     * @return {@code true} if the token has expired
     */
    private boolean isExpired(String token){
        return parseClaims(token).getExpiration().before(new Date());
    }

    /**
     * Verifies the token's signature and returns its claims.
     *
     * @param token the signed JWT
     * @return the parsed claims payload
     */
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
