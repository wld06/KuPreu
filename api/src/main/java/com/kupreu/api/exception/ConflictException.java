package com.kupreu.api.exception;

/**
 * Thrown when a request conflicts with the current state (e.g. a duplicate).
 * Mapped to HTTP 409 by {@code GlobalExceptionHandler}.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message human-readable explanation of the conflict
     */
    public ConflictException(String message) {
        super(message);
    }
}
