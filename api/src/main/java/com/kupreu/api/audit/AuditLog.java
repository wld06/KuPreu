package com.kupreu.api.audit;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a single audit-log entry, stored in the
 * {@code audit_log} collection. Entries are immutable once created.
 */
@Document(collection = "audit_log")
@Getter
@Builder
public class AuditLog {
    /** MongoDB document identifier. */
    @Id
    private String id;

    /** Instant at which the audited action occurred. */
    private Instant timestamp;

    /** Machine-readable action code (e.g. {@code BRAND_CREATED}). */
    private String action;

    /** Identity of the user or process that performed the action. */
    private String actor;

    /** Short human-readable description of what happened. */
    private String message;

    /** Optional additional context (ids, values, error class, etc.). */
    private String detail;

    /** Whether the audited action succeeded. */
    private boolean success;
}
