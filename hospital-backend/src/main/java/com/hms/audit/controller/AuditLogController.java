
package com.hms.audit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hms.audit.dto.AuditLogRequest;
import com.hms.audit.dto.AuditLogResponse;
import com.hms.audit.service.AuditLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<AuditLogResponse> createAuditLog(
            @Valid @RequestBody AuditLogRequest request) {

        AuditLogResponse response =
                auditLogService.createAuditLog(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {

        return ResponseEntity.ok(
                auditLogService.getAllAuditLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLogById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                auditLogService.getAuditLogById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                auditLogService.getAuditLogsByUser(userId));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(
            @PathVariable String action) {

        return ResponseEntity.ok(
                auditLogService.getAuditLogsByAction(action));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {

        return ResponseEntity.ok(
                auditLogService.getAuditLogsByEntity(
                        entityType, entityId));
    }
}
