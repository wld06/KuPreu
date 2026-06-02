package com.kupreu.api.config.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter{
    private final ProxyManager<byte[]> buckets;

    public RateLimitFilter(RedisClient redisClient){
        this.buckets = LettuceBasedProxyManager
                        .builderFor(redisClient)
                        .build();
    }

    private BucketConfiguration bucketConfig(){
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                            .capacity(20)
                            .refillGreedy(20, Duration.ofMinutes(1))
                            .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();

        byte[] key = ("rate_limit:" + ip).getBytes(StandardCharsets.UTF_8);

        Bucket bucket = buckets.builder()
                        .build(key, this::bucketConfig);
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }
}
