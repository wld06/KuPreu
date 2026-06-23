package com.kupreu.api.audit;

import com.kupreu.api.DTOs.AuditResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import org.slf4j.Logger;

/**
 * Service for writing and querying audit entries. Every record is logged via SLF4J
 * and best-effort persisted to MongoDB; a persistence failure is logged but never
 * propagated, so auditing cannot break the main business flow.
 */
@Service
@RequiredArgsConstructor
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;


    /**
     * Returns a page of all audit entries, most recent first.
     *
     * @param page zero-based page index
     * @param size page size
     * @return a page of audit responses
     */
    public Page<AuditResponse> getAll(int page, int size){
        return repository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    /**
     * Returns a page of failed audit entries, most recent first.
     *
     * @param page zero-based page index
     * @param size page size
     * @return a page of error audit responses
     */
    public Page<AuditResponse> getErrors(int page, int size){
        return repository.findBySuccessOrderByTimestampDesc(false, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    /**
     * Searches audit entries by a free-text term across action, actor and message.
     *
     * @param term the search term
     * @param page zero-based page index
     * @param size page size
     * @return a page of matching audit responses
     */
    public Page<AuditResponse> search(String term, int page, int size){
        return repository.search(term, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    /**
     * Resolves the current authenticated principal from the security context.
     * Returns "anonymous" when no authentication is present.
     *
     * @return the current actor's name, or {@code "anonymous"}
     */
    public String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || !auth.isAuthenticated()) ? "anonymous" : auth.getName();
    }

    /**
     * Records an audit entry, resolving the actor automatically from the security context.
     *
     * @param action  machine-readable action code
     * @param message short human-readable description
     * @param detail  optional additional context
     * @param success whether the action succeeded
     */
    public void record(String action, String message, String detail, boolean success){
        record(action, currentActor(), message, detail, success);
    }

    /**
     * Records an audit entry for an explicit actor. The entry is always logged and
     * best-effort persisted; persistence failures are swallowed after being logged.
     *
     * @param action  machine-readable action code
     * @param actor   identity that performed the action
     * @param message short human-readable description
     * @param detail  optional additional context
     * @param success whether the action succeeded
     */
    public void record(String action, String actor, String message, String detail, boolean success){
        AuditLog entry = AuditLog.builder()
                .timestamp(Instant.now())
                .action(action)
                .actor(actor)
                .message(message)
                .detail(detail)
                .success(success)
                .build();

        if (success){
            log.info("AUDIT {} actor={} - {}", action, actor, message);
        } else {
            log.warn("AUDIT {} actor={} - {} ({})", action, actor, message, detail);
        }

        try{
            repository.save(entry);
        } catch (Exception e){
            log.error("Failed to persist audit log to Mongo: {}", e.getMessage());
        }
    }

    /** Maps an {@link AuditLog} document to its response DTO. */
    private AuditResponse toResponse(AuditLog log){
        return AuditResponse.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .action(log.getAction())
                .actor(log.getActor())
                .message(log.getMessage())
                .detail(log.getDetail())
                .success(log.isSuccess())
                .build();
    }
}
