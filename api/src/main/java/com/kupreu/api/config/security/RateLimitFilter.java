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

/**
 * Per-request filter that throttles authentication endpoints by client IP.
 * It uses Bucket4j token buckets backed by Redis so limits are shared across
 * instances. Only requests under {@code /api/auth} are rate limited.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter{
    /** Redis-backed store of per-IP token buckets. */
    private final ProxyManager<byte[]> buckets;
    /** Configurable capacity and refill settings for each bucket. */
    private final RateLimitProperties props;

    /**
     * Builds the filter and its Redis-backed bucket store.
     *
     * @param redisClient the Lettuce client used to persist bucket state
     * @param props       the rate-limit capacity and refill configuration
     */
    public RateLimitFilter(RedisClient redisClient, RateLimitProperties props) {
        this.buckets = LettuceBasedProxyManager
                        .builderFor(redisClient)
                        .build();
        this.props = props;
    }

    /**
     * Builds the bucket configuration from the configured capacity and refill rate.
     *
     * @return the configuration applied to each per-IP bucket
     */
    private BucketConfiguration bucketConfig(){
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                            .capacity(props.getCapacity())
                            .refillGreedy(props.getRefillToken(), props.getRefillPeriod())
                            .build())
                .build();
    }

    /**
     * Consumes one token from the caller's bucket for {@code /api/auth} requests.
     * Responds with HTTP 429 when the bucket is empty and HTTP 503 if the bucket
     * store cannot be reached; other requests bypass the limiter.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response
     * @param chain    the remaining filter chain
     * @throws ServletException if the chain fails
     * @throws IOException      if an I/O error occurs while processing the chain
     */
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
