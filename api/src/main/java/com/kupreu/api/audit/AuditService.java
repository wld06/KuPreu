package com.kupreu.api.audit;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import org.slf4j.Logger;

@Service
@RequiredArgsConstructor
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;

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
}
