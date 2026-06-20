package com.kupreu.api.audit;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_log")
@Getter
@Builder
public class AuditLog {
    @Id
    private String id;

    private Instant timestamp;

    private String action;
    private String actor;
    private String message;
    private String detail;
    private boolean success;
}
