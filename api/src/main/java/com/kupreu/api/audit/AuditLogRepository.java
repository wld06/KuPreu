package com.kupreu.api.audit;

import com.kupreu.api.DTOs.AuditResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for {@link AuditLog} documents.
 */
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    /**
     * Finds all audit entries produced by a given actor.
     *
     * @param actor the actor identity
     * @return the matching entries
     */
    List<AuditLog> findByActor(String actor);

    /**
     * Finds all entries for a given action, most recent first.
     *
     * @param action the action code
     * @return the matching entries ordered by timestamp descending
     */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    /**
     * Returns a page of all audit entries, most recent first.
     *
     * @param pageable pagination information
     * @return a page of audit entries
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Returns a page of audit entries filtered by success flag, most recent first.
     *
     * @param success  whether to return successful ({@code true}) or failed entries
     * @param pageable pagination information
     * @return a page of matching audit entries
     */
    Page<AuditLog> findBySuccessOrderByTimestampDesc(boolean success, Pageable pageable);

    /**
     * Case-insensitively searches the action, actor and message fields for the term.
     *
     * @param term     the search term (used as a regular expression)
     * @param pageable pagination information
     * @return a page of matching audit entries
     */
    @Query("{ $or: [ " +
            "{ 'action':  { $regex: ?0, $options: 'i' } }, " +
            "{ 'actor':   { $regex: ?0, $options: 'i' } }, " +
            "{ 'message': { $regex: ?0, $options: 'i' } } ] }")
    Page<AuditLog> search(String term, Pageable pageable);
}
