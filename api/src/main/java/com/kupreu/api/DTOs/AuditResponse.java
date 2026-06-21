package com.kupreu.api.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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
