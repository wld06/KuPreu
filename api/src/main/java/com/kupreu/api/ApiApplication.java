package com.kupreu.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the KuPreu API.
 * Enables component scanning and binding of {@code @ConfigurationProperties} classes.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiApplication {

	/**
	 * Boots the Spring application context.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
