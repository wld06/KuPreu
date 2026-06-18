package com.kupreu.api.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kupreu.api.config.RateLimitProperties;

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

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter{
    private final ProxyManager<byte[]> buckets;
    private final RateLimitProperties props;

    public RateLimitFilter(RedisClient redisClient, RateLimitProperties props) {
        this.buckets = LettuceBasedProxyManager
                        .builderFor(redisClient)
                        .build();
        this.props = props;
    }

    private BucketConfiguration bucketConfig(){
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                            .capacity(props.getCapacity())
                            .refillGreedy(props.getRefillToken(), props.getRefillPeriod())
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

        // Real client IP: server.forward-headers-strategy=framework makes Spring
        // rewrite getRemoteAddr() from X-Forwarded-For/Forwarded when behind a proxy.
        String ip = request.getRemoteAddr();
        boolean allowed;
        byte[] key = ("rate_limit:" + ip).getBytes(StandardCharsets.UTF_8);

        try{
            Bucket bucket = buckets.builder()
                    .build(key, this::bucketConfig);
            allowed = bucket.tryConsume(1);
        } catch (Exception e){
            //log
            response.setStatus(503); //service unavailable
            response.setHeader("Retry-After", "5"); //seconds
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Service temporarily unavailable\"}");
            return;
        }

        if (allowed){
            chain.doFilter(request, response);
        } else{
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }
}
