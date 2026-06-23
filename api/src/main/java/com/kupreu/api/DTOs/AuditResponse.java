package com.kupreu.api.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Response payload representing a single audit-log entry returned to administrators.
 */
@Data
@Builder
public class AuditResponse {
    private String id;
    private Instant timestamp;
    private String action;
    private String actor;
    private String message;
    private String detail;
    private boolean success;
}
