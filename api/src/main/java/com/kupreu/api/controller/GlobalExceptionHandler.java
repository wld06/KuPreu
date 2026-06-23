package com.kupreu.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kupreu.api.audit.AuditService;
import com.kupreu.api.exception.BadRequestException;
import com.kupreu.api.exception.ConflictException;
import com.kupreu.api.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Centralized exception handler that translates application and framework exceptions
 * into consistent JSON error responses with the appropriate HTTP status. Security and
 * unexpected failures are also recorded through the {@link AuditService}.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;

    /**
     * Handles authorization failures, returning HTTP 403 and auditing the denial.
     *
     * @param e the access-denied exception
     * @return HTTP 403 with a generic error body
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        auditService.record("ACCESS_DENIED", auditService.currentActor(),
                "Access denied", e.getMessage(), false);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    /**
     * Handles failed authentication, returning HTTP 401 and auditing the failure.
     *
     * @param e the authentication exception
     * @return HTTP 401 with a generic error body
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException e) {
        auditService.record("AUTHENTICATION_FAILED", auditService.currentActor(),
                "Invalid credentials", e.getClass().getSimpleName(), false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }

    /**
     * Handles missing-resource errors, returning HTTP 404 with the exception message.
     *
     * @param e the not-found exception
     * @return HTTP 404 with the error message
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles state-conflict errors (e.g. duplicates), returning HTTP 409.
     *
     * @param e the conflict exception
     * @return HTTP 409 with the error message
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles invalid-input errors raised by the application, returning HTTP 400.
     *
     * @param e the bad-request exception
     * @return HTTP 400 with the error message
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Handles bean-validation failures on request bodies, returning HTTP 400 with a
     * field-to-message map describing each violation.
     *
     * @param e the validation exception
     * @return HTTP 400 with per-field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles malformed or missing request bodies, returning HTTP 400.
     *
     * @param e the message-not-readable exception
     * @return HTTP 400 with a generic error body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid or missing request body"));
    }

    /**
     * Catch-all for unhandled runtime errors, returning HTTP 500 and auditing the
     * failure without exposing internal details to the client.
     *
     * @param e the unexpected runtime exception
     * @return HTTP 500 with a generic error body
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        auditService.record("UNHANDLED_ERROR", auditService.currentActor(),
                "Unexpected error: " + e.getClass().getSimpleName(), e.getMessage(), false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
