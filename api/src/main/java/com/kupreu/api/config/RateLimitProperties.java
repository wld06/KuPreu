package com.kupreu.api.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {
    private int capacity = 20;
    private int refillToken = 20;
    private Duration refillPeriod = Duration.ofMinutes(1);

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillToken() {
        return refillToken;
    }
    public void setRefillToken(int refillToken) {
        this.refillToken = refillToken;
    }
    
    public Duration getRefillPeriod() {
        return refillPeriod;
    }
    public void setRefillPeriod(Duration refillPeriod) {
        this.refillPeriod = refillPeriod;
    }

}
