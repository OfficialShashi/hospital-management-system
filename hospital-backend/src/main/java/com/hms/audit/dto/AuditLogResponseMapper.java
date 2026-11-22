
package com.hms.audit.dto;

import org.springframework.stereotype.Component;

import com.hms.audit.AuditLog;

@Component
public class AuditLogResponseMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {

        AuditLogResponse response = new AuditLogResponse();

        response.setAuditId(auditLog.getAuditId());

        if (auditLog.getUser() != null) {
            response.setUserId(
                    auditLog.getUser().getUserId());

            response.setUserName(
                    auditLog.getUser().getFullName());
        }

        response.setAction(auditLog.getAction());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setDescription(auditLog.getDescription());
        response.setIpAddress(auditLog.getIpAddress());
        response.setCreatedAt(auditLog.getCreatedAt());

        return response;
    }
}
