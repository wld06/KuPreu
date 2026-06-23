package com.kupreu.api.exception;

/**
 * Thrown when a requested resource does not exist.
 * Mapped to HTTP 404 by {@code GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message human-readable explanation of what was not found
     */
    public NotFoundException(String message) {
        super(message);
    }
}
