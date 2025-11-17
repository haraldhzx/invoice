package com.invoiceapp.service;

import com.invoiceapp.model.entity.AuditLog;
import com.invoiceapp.model.entity.AuditAction;
import com.invoiceapp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logSecurityEvent(AuditAction action, UUID userId, String details, HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .userId(userId)
                    .details(details)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Security event logged: {} for user {}", action, userId);
        } catch (Exception e) {
            log.error("Failed to log security event", e);
        }
    }

    @Async
    @Transactional
    public void logLogin(UUID userId, boolean success, HttpServletRequest request) {
        String details = success ? "Successful login" : "Failed login attempt";
        logSecurityEvent(AuditAction.LOGIN, userId, details, request);
    }

    @Async
    @Transactional
    public void logLogout(UUID userId, HttpServletRequest request) {
        logSecurityEvent(AuditAction.LOGOUT, userId, "User logged out", request);
    }

    @Async
    @Transactional
    public void logPasswordChange(UUID userId, HttpServletRequest request) {
        logSecurityEvent(AuditAction.PASSWORD_CHANGE, userId, "Password changed", request);
    }

    @Async
    @Transactional
    public void logSuspiciousActivity(UUID userId, String activity, HttpServletRequest request) {
        logSecurityEvent(AuditAction.SUSPICIOUS_ACTIVITY, userId, activity, request);
        log.warn("Suspicious activity detected: {} for user {}", activity, userId);
    }

    @Async
    @Transactional
    public void logDataAccess(UUID userId, String resourceType, UUID resourceId, HttpServletRequest request) {
        String details = String.format("Accessed %s: %s", resourceType, resourceId);
        logSecurityEvent(AuditAction.DATA_ACCESS, userId, details, request);
    }

    @Async
    @Transactional
    public void logDataModification(UUID userId, String resourceType, UUID resourceId, String action, HttpServletRequest request) {
        String details = String.format("%s %s: %s", action, resourceType, resourceId);
        logSecurityEvent(AuditAction.DATA_MODIFICATION, userId, details, request);
    }

    @Async
    @Transactional
    public void logSecurityViolation(UUID userId, String violationType, HttpServletRequest request) {
        String details = String.format("Security violation: %s", violationType);
        logSecurityEvent(AuditAction.SECURITY_VIOLATION, userId, details, request);
        log.error("Security violation detected: {} for user {}", violationType, userId);
    }

    /**
     * Get real client IP address, considering proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Detect anomalous login patterns
     */
    public boolean isAnomalousLogin(UUID userId, String ipAddress) {
        // Check for multiple failed login attempts
        long recentFailures = auditLogRepository.countRecentFailedLogins(userId, LocalDateTime.now().minusMinutes(15));
        if (recentFailures >= 5) {
            return true;
        }

        // Check for login from unusual location (different IP within short time)
        return auditLogRepository.hasRecentLoginFromDifferentIp(userId, ipAddress, LocalDateTime.now().minusHours(1));
    }

    /**
     * Check if user account should be locked due to suspicious activity
     */
    public boolean shouldLockAccount(UUID userId) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        long violations = auditLogRepository.countSecurityViolations(userId, cutoff);
        return violations >= 10;
    }

    /**
     * Get security summary for user
     */
    public Map<String, Object> getSecuritySummary(UUID userId) {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLogins", auditLogRepository.countLogins(userId, last30Days));
        summary.put("failedLogins", auditLogRepository.countFailedLogins(userId, last30Days));
        summary.put("passwordChanges", auditLogRepository.countPasswordChanges(userId, last30Days));
        summary.put("securityViolations", auditLogRepository.countSecurityViolations(userId, last30Days));
        summary.put("lastLoginDate", auditLogRepository.getLastLoginDate(userId));
        summary.put("lastLoginIp", auditLogRepository.getLastLoginIp(userId));

        return summary;
    }
}
