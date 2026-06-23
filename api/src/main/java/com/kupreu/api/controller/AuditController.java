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

/**
 * REST controller exposing audit-log endpoints under {@code /api/audit}.
 * The whole controller is restricted to users with the {@code ADMIN} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditService service;

    /**
     * Returns a paginated view of all audit entries.
     *
     * @param page zero-based page index
     * @param size page size
     * @return HTTP 200 with a page of audit entries
     */
    @GetMapping
    public ResponseEntity<Page<AuditResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(service.getAll(page, size));
    }

    /**
     * Returns a paginated view of audit entries that represent failures.
     *
     * @param page zero-based page index
     * @param size page size
     * @return HTTP 200 with a page of error audit entries
     */
    @GetMapping("/errors")
    public ResponseEntity<Page<AuditResponse>> getErrors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(service.getErrors(page, size));
    }

    /**
     * Searches audit entries by a free-text query.
     *
     * @param q    the search term
     * @param page zero-based page index
     * @param size page size
     * @return HTTP 200 with a page of matching audit entries
     */
    @GetMapping("/search")
    public ResponseEntity<Page<AuditResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(service.search(q, page, size));
    }
}
