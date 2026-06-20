package com.kupreu.api.audit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByActor(String actor);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
