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

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditService auditService;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        auditService.record("ACCESS_DENIED", auditService.currentActor(),
                "Access denied", e.getMessage(), false);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException e) {
        auditService.record("AUTHENTICATION_FAILED", auditService.currentActor(),
                "Invalid credentials", e.getClass().getSimpleName(), false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid or missing request body"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        auditService.record("UNHANDLED_ERROR", auditService.currentActor(),
                "Unexpected error: " + e.getClass().getSimpleName(), e.getMessage(), false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}
