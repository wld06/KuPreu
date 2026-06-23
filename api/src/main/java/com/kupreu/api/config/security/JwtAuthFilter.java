package com.kupreu.api.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Per-request filter that authenticates callers from a Bearer JWT.
 * When a valid token is present it loads the user and populates the
 * {@link SecurityContextHolder}; requests without a token pass through
 * unauthenticated and any failure clears the security context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Reads the {@code Authorization} header, validates the Bearer token and,
     * on success, stores the authenticated user in the security context before
     * continuing the filter chain.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if the chain fails
     * @throws IOException      if an I/O error occurs while processing the chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
       try{
           String authHeader = request.getHeader("Authorization");

           if (authHeader == null || !authHeader.startsWith("Bearer ")){
               filterChain.doFilter(request, response);
               return;
           }

           String token = authHeader.substring(7);
           String email = jwtProvider.extractEmail(token);

           if (email != null && SecurityContextHolder.getContext().getAuthentication() == null){
               UserDetails userDetails = userDetailsService.loadUserByUsername(email);

               if (jwtProvider.isTokenValid(token, email)){
                   UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                   authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(authToken);
               }
           }

           filterChain.doFilter(request, response);
       } catch (Exception ex){
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
       }
    }
}
