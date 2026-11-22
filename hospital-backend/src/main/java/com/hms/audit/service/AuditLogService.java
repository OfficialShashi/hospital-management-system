
package com.hms.audit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hms.audit.AuditLog;
import com.hms.audit.dto.AuditLogRequest;
import com.hms.audit.dto.AuditLogResponse;
import com.hms.audit.dto.AuditLogResponseMapper;
import com.hms.audit.repository.AuditLogRepository;
import com.hms.user.User;
import com.hms.user.repository.UserRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogResponseMapper responseMapper;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            AuditLogResponseMapper responseMapper) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional
    public AuditLogResponse createAuditLog(AuditLogRequest request) {

        AuditLog auditLog = new AuditLog();

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("User not found"));

            auditLog.setUser(user);
        }

        auditLog.setAction(request.getAction().trim().toUpperCase());
        auditLog.setEntityType(request.getEntityType().trim().toUpperCase());
        auditLog.setEntityId(request.getEntityId());
        auditLog.setDescription(request.getDescription());
        auditLog.setIpAddress(request.getIpAddress());

        AuditLog savedLog = auditLogRepository.save(auditLog);

        return responseMapper.toResponse(savedLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long auditId) {

        AuditLog auditLog = auditLogRepository.findById(auditId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Audit log not found"));

        return responseMapper.toResponse(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {

        return auditLogRepository.findByUser_UserId(userId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByAction(String action) {

        return auditLogRepository.findByAction(
                action.trim().toUpperCase())
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEntity(
            String entityType, Long entityId) {

        return auditLogRepository
                .findByEntityTypeAndEntityId(
                        entityType.trim().toUpperCase(), entityId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }
}
