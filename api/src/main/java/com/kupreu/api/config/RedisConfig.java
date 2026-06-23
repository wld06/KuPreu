package com.kupreu.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

/**
 * Spring configuration that builds the Lettuce {@link RedisClient} used by the rate
 * limiter. Connection details are read from {@code spring.data.redis.*} with sensible
 * localhost defaults.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * Creates the Redis client, applying a password only when one is configured.
     * The client is shut down with the application context.
     *
     * @return a configured {@link RedisClient}
     */
    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI.Builder uri = RedisURI.builder()
                .withHost(host)
                .withPort(port);
        if (password != null && !password.isBlank()) {
            uri.withPassword(password.toCharArray());
        }
        return RedisClient.create(uri.build());
    }
}
