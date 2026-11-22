
package com.hms.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.audit.AuditLog;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<AuditLog> findAll();

    @EntityGraph(attributePaths = {"user"})
    List<AuditLog> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<AuditLog> findByAction(String action);

    @EntityGraph(attributePaths = {"user"})
    List<AuditLog> findByEntityType(String entityType);

    @EntityGraph(attributePaths = {"user"})
    List<AuditLog> findByEntityTypeAndEntityId(
            String entityType, Long entityId);
}
