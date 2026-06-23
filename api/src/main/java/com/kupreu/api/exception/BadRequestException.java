package com.kupreu.api.exception;

/**
 * Thrown when a request is syntactically valid but semantically invalid.
 * Mapped to HTTP 400 by {@code GlobalExceptionHandler}.
 */
public class BadRequestException extends RuntimeException {

    /**
     * @param message human-readable explanation of why the request is invalid
     */
    public BadRequestException(String message) {
        super(message);
    }
}
