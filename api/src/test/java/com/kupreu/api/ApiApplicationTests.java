package com.kupreu.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kupreu.api.config.security.RateLimitFilter;

@SpringBootTest
class ApiApplicationTests {

	// RateLimitFilter opens a Redis connection in its constructor; mock it so the
	// context can load without a running Redis instance.
	@MockitoBean
	private RateLimitFilter rateLimitFilter;

	@Test
	void contextLoads() {
	}

}
