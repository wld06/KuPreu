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

@Service
@RequiredArgsConstructor
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;


    public Page<AuditResponse> getAll(int page, int size){
        return repository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public Page<AuditResponse> getErrors(int page, int size){
        return repository.findBySuccessOrderByTimestampDesc(false, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public Page<AuditResponse> search(String term, int page, int size){
        return repository.search(term, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    /**
     * Resolves the current authenticated principal from the security context.
     * Returns "anonymous" when no authentication is present.
     */
    public String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || !auth.isAuthenticated()) ? "anonymous" : auth.getName();
    }

    /**
     * Records an audit entry resolving the actor automatically from the security context.
     */
    public void record(String action, String message, String detail, boolean success){
        record(action, currentActor(), message, detail, success);
    }

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
