package com.kupreu.api.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the request rate limiter, bound from the
 * {@code rate.limit.*} keys. Defaults allow 20 requests per minute per client.
 */
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    /** Maximum number of tokens (requests) a bucket can hold. */
    private int capacity = 20;
    /** Number of tokens added to the bucket each refill period. */
    private int refillToken = 20;
    /** Time window over which tokens are refilled. */
    private Duration refillPeriod = Duration.ofMinutes(1);

    /** @return the bucket capacity */
    public int getCapacity() {
        return capacity;
    }
    /** @param capacity the bucket capacity to set */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /** @return the number of tokens added per refill period */
    public int getRefillToken() {
        return refillToken;
    }
    /** @param refillToken the number of tokens added per refill period to set */
    public void setRefillToken(int refillToken) {
        this.refillToken = refillToken;
    }

    /** @return the refill period */
    public Duration getRefillPeriod() {
        return refillPeriod;
    }
    /** @param refillPeriod the refill period to set */
    public void setRefillPeriod(Duration refillPeriod) {
        this.refillPeriod = refillPeriod;
    }

}
