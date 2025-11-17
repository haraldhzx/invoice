package com.invoiceapp.security;

import com.invoiceapp.service.SecurityAuditService;
import com.invoiceapp.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestValidationInterceptor implements HandlerInterceptor {

    private final InputSanitizer inputSanitizer;
    private final SecurityAuditService securityAuditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Validate all request parameters
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);

            if (paramValue != null) {
                // Check for malicious patterns
                if (!inputSanitizer.isSafe(paramValue)) {
                    log.warn("Malicious input detected in parameter '{}': {}", paramName, paramValue);

                    // Log security violation
                    String userId = request.getRemoteUser();
                    if (userId != null) {
                        try {
                            securityAuditService.logSecurityViolation(
                                    UUID.fromString(userId),
                                    "Malicious input in parameter: " + paramName,
                                    request
                            );
                        } catch (Exception e) {
                            log.error("Failed to log security violation", e);
                        }
                    }

                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.getWriter().write("{\"error\":\"Invalid input detected\"}");
                    response.setContentType("application/json");
                    return false;
                }

                // Validate specific parameter types
                if (paramName.toLowerCase().contains("email") && !paramValue.isEmpty()) {
                    if (!inputSanitizer.isValidEmail(paramValue)) {
                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                        response.getWriter().write("{\"error\":\"Invalid email format\"}");
                        response.setContentType("application/json");
                        return false;
                    }
                }

                if (paramName.toLowerCase().contains("id") && !paramValue.isEmpty()) {
                    if (!inputSanitizer.isValidUuid(paramValue)) {
                        response.setStatus(HttpStatus.BAD_REQUEST.value());
                        response.getWriter().write("{\"error\":\"Invalid ID format\"}");
                        response.setContentType("application/json");
                        return false;
                    }
                }
            }
        }

        // Validate headers for injection attempts
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !inputSanitizer.isSafe(userAgent)) {
            log.warn("Malicious User-Agent detected: {}", userAgent);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }

        // Check for directory traversal in path
        String path = request.getRequestURI();
        if (path.contains("../") || path.contains("..\\")) {
            log.warn("Directory traversal attempt detected: {}", path);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }

        return true;
    }
}
