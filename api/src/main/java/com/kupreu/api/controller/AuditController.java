package com.kupreu.api.controller;

import com.kupreu.api.DTOs.AuditResponse;
import com.kupreu.api.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditService service;

    @GetMapping
    public ResponseEntity<Page<AuditResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @GetMapping("/errors")
    public ResponseEntity<Page<AuditResponse>> getErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(service.getErrors(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuditResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(service.search(q, page, size));
    }
}
