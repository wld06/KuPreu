package com.kupreu.api.audit;

import com.kupreu.api.DTOs.AuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByActor(String actor);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    Page<AuditLog> findBySuccessOrderByTimestampDesc(boolean success, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'action':  { $regex: ?0, $options: 'i' } }, " +
            "{ 'actor':   { $regex: ?0, $options: 'i' } }, " +
            "{ 'message': { $regex: ?0, $options: 'i' } } ] }")
    Page<AuditLog> search(String term, Pageable pageable);
}
